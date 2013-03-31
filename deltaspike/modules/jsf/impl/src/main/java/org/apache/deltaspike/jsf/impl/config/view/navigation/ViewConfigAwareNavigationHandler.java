/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.jsf.impl.config.view.navigation;

import org.apache.deltaspike.core.api.config.view.DefaultErrorView;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameter;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.core.api.config.view.navigation.event.PreViewConfigNavigateEvent;
import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameterContext;
import org.apache.deltaspike.jsf.impl.util.JsfUtils;

import javax.enterprise.inject.spi.BeanManager;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ViewConfigAwareNavigationHandler extends NavigationHandler
{
    private Set<String> otherOutcomes = new CopyOnWriteArraySet<String>();
    private Map<String, ViewConfigDescriptor> viewConfigs = new ConcurrentHashMap<String, ViewConfigDescriptor>();

    private final NavigationHandler navigationHandler;

    private BeanManager beanManager;

    private NavigationParameterContext navigationParameterContext;

    /**
     * Constructor which allows to use the given {@link NavigationHandler}
     *
     * @param navigationHandler navigation-handler of jsf
     */
    public ViewConfigAwareNavigationHandler(NavigationHandler navigationHandler)
    {
        this.navigationHandler = navigationHandler;
    }

    //Security checks will be performed by the view-handler provided by ds
    @Override
    public void handleNavigation(FacesContext facesContext, String fromAction, String outcome)
    {
        initBeanManager();
        if (outcome != null && outcome.contains("."))
        {
            String originalOutcome = outcome;

            if (!this.otherOutcomes.contains(outcome))
            {
                //it isn't possible to support interfaces due to cdi restrictions
                if (outcome.startsWith("class "))
                {
                    outcome = outcome.substring(6);
                }
                ViewConfigDescriptor entry = this.viewConfigs.get(outcome);

                if (entry == null)
                {
                    if (DefaultErrorView.class.getName().equals(originalOutcome))
                    {
                        entry = JsfUtils.getViewConfigResolver().getDefaultErrorViewConfigDescriptor();
                    }
                }

                boolean allowCaching = true;
                if (entry == null)
                {
                    Class<?> loadedClass = ClassUtils.tryToLoadClassForName(outcome);

                    if (loadedClass == null)
                    {
                        this.otherOutcomes.add(originalOutcome);
                    }
                    else if (ViewConfig.class.isAssignableFrom(loadedClass))
                    {
                        //a sub-classed page-config for annotating it with different view params
                        if (loadedClass.getAnnotation(View.class) == null &&
                                loadedClass.getSuperclass().getAnnotation(View.class) != null)
                        {
                            allowCaching = false;
                            addConfiguredViewParameters(loadedClass);

                            loadedClass = loadedClass.getSuperclass();
                        }
                        entry = JsfUtils.getViewConfigResolver()
                                .getViewConfigDescriptor((Class<? extends ViewConfig>) loadedClass);
                    }
                }

                if (entry != null)
                {
                    //in case of false it has been added already
                    if (allowCaching)
                    {
                        this.viewConfigs.put(outcome, entry);
                        addConfiguredViewParameters(entry.getConfigClass());
                    }

                    String oldViewId = null;

                    if (facesContext.getViewRoot() != null)
                    {
                        oldViewId = facesContext.getViewRoot().getViewId();
                    }

                    PreViewConfigNavigateEvent navigateEvent = firePreViewConfigNavigateEvent(oldViewId, entry);

                    entry = tryToUpdateEntry(entry, navigateEvent);

                    if (entry != null)
                    {
                        outcome = convertEntryToOutcome(facesContext.getExternalContext(), entry);
                    }
                }
            }
        }

        this.navigationHandler.handleNavigation(facesContext, fromAction, outcome);
    }

    private void addConfiguredViewParameters(Class<?> viewConfigClass)
    {
        if (this.navigationParameterContext != null)
        {
            NavigationParameter navigationParameter = viewConfigClass.getAnnotation(NavigationParameter.class);

            if (navigationParameter != null)
            {
                addConfiguredPageParameter(navigationParameter);
            }
            else
            {
                NavigationParameter.List pageParameterList =
                        viewConfigClass.getAnnotation(NavigationParameter.List.class);

                if (pageParameterList != null)
                {
                    for (NavigationParameter currentNavigationParameter : pageParameterList.value())
                    {
                        addConfiguredPageParameter(currentNavigationParameter);
                    }
                }
            }
        }
    }

    private void addConfiguredPageParameter(NavigationParameter viewParameter)
    {
        this.navigationParameterContext.addPageParameter(viewParameter.key(), viewParameter.value());
    }

    private String convertEntryToOutcome(ExternalContext externalContext, ViewConfigDescriptor entry)
    {
        View viewMetaData = entry.getMetaData(View.class).iterator().next();

        boolean performRedirect = View.NavigationMode.REDIRECT.equals(viewMetaData.navigation());
        boolean includeViewParameters = View.ViewParameterMode.INCLUDE.equals(viewMetaData.viewParams());

        StringBuilder result = new StringBuilder(entry.getViewId());

        if (performRedirect)
        {
            result.append("?faces-redirect=true");
        }
        if (includeViewParameters)
        {
            if (performRedirect)
            {
                result.append("&");
            }
            else
            {
                result.append("?");
            }
            result.append("includeViewParams=true");

            return JsfUtils.addPageParameters(externalContext, result.toString(), false);
        }

        return result.toString();
    }

    private ViewConfigDescriptor tryToUpdateEntry(ViewConfigDescriptor viewConfigDescriptor,
                                                  PreViewConfigNavigateEvent navigateEvent)
    {
        if (navigateEvent == null)
        {
            return viewConfigDescriptor;
        }

        if (navigateEvent.getToView() == null)
        {
            return null;
        }

        if (navigateEvent.getToView().equals(viewConfigDescriptor.getConfigClass()))
        {
            return viewConfigDescriptor;
        }

        return JsfUtils.getViewConfigResolver().getViewConfigDescriptor(navigateEvent.getToView());
    }

    private PreViewConfigNavigateEvent firePreViewConfigNavigateEvent(String oldViewId,
                                                                      ViewConfigDescriptor newViewConfigDescriptor)
    {
        ViewConfigResolver viewConfigResolver = JsfUtils.getViewConfigResolver();

        ViewConfigDescriptor oldViewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(oldViewId);

        if (oldViewConfigDescriptor != null)
        {
            PreViewConfigNavigateEvent navigateEvent = new PreViewConfigNavigateEvent(
                    oldViewConfigDescriptor.getConfigClass(), newViewConfigDescriptor.getConfigClass());

            this.beanManager.fireEvent(navigateEvent);
            return navigateEvent;
        }
        return null;
    }

    private void initBeanManager()
    {
        if (this.beanManager == null)
        {
            this.beanManager = BeanManagerProvider.getInstance().getBeanManager();
            this.navigationParameterContext =
                    BeanProvider.getContextualReference(NavigationParameterContext.class);
        }
    }
}
