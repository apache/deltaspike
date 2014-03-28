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
package org.apache.deltaspike.jsf.impl.navigation;

import org.apache.deltaspike.core.api.config.view.DefaultErrorView;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.jsf.impl.config.view.navigation.NavigationCaseMapWrapper;
import org.apache.deltaspike.jsf.impl.config.view.navigation.ViewConfigAwareNavigationHandler;
import org.apache.deltaspike.jsf.impl.util.JsfUtils;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.apache.deltaspike.jsf.impl.scope.viewaccess.ViewAccessScopedAwareNavigationHandler;

public class DeltaSpikeNavigationHandler extends ConfigurableNavigationHandler implements Deactivatable
{
    private Set<String> otherOutcomes = new CopyOnWriteArraySet<String>();

    private Map<String, NavigationCase> viewConfigBasedNavigationCaseCache
        = new ConcurrentHashMap<String, NavigationCase>();

    private final NavigationHandler wrapped;
    private final boolean activated;
    private final boolean vasnhActivated;

    /**
     * Constructor for wrapping the given {@link NavigationHandler}
     *
     * @param navigationHandler navigation-handler which should be wrapped
     */
    public DeltaSpikeNavigationHandler(NavigationHandler navigationHandler)
    {
        this.wrapped = navigationHandler;
        this.activated = ClassDeactivationUtils.isActivated(getClass());
        this.vasnhActivated = ClassDeactivationUtils.isActivated(ViewAccessScopedAwareNavigationHandler.class);
    }

    @Override
    public void handleNavigation(FacesContext context, String fromAction, String outcome)
    {
        if (!this.activated || isUnhandledExceptionQueued(context))
        {
            this.wrapped.handleNavigation(context, fromAction, outcome);
        }
        else
        {
            //don't refactor it - currently we need the lazy wrapping due to special jsf2 constellations
            getWrappedNavigationHandler().handleNavigation(context, fromAction, outcome);
        }
    }

    private boolean isUnhandledExceptionQueued(FacesContext context)
    {
        return context.getExceptionHandler().getUnhandledExceptionQueuedEvents() != null &&
                context.getExceptionHandler().getUnhandledExceptionQueuedEvents().iterator().hasNext();
    }

    private NavigationHandler getWrappedNavigationHandler()
    {
        NavigationHandler navigationHandler = new ViewConfigAwareNavigationHandler(this.wrapped);

        if (vasnhActivated)
        {
            navigationHandler = new ViewAccessScopedAwareNavigationHandler(navigationHandler);
        }

        return navigationHandler;
    }

    @Override
    public NavigationCase getNavigationCase(FacesContext context, String action, String outcome)
    {
        if (this.wrapped instanceof ConfigurableNavigationHandler)
        {
            if (!this.activated)
            {
                return ((ConfigurableNavigationHandler)this.wrapped).getNavigationCase(context, action, outcome);
            }

            if (action == null && outcome != null && outcome.contains(".") && outcome.startsWith("class ") &&
                    !otherOutcomes.contains(outcome))
            {
                String originalOutcome = outcome;

                NavigationCase navigationCase = this.viewConfigBasedNavigationCaseCache.get(originalOutcome);

                if (navigationCase != null)
                {
                    return navigationCase;
                }

                outcome = outcome.substring(6);

                ViewConfigDescriptor entry = null;

                if (DefaultErrorView.class.getName().equals(originalOutcome))
                {
                    ViewConfigResolver viewConfigResolver = JsfUtils.getViewConfigResolver();
                    entry = viewConfigResolver.getDefaultErrorViewConfigDescriptor();
                }

                if (entry == null)
                {
                    Object loadedClass = ClassUtils.tryToLoadClassForName(outcome);

                    if (loadedClass == null)
                    {
                        this.otherOutcomes.add(originalOutcome);
                    }
                    else if (ViewConfig.class.isAssignableFrom((Class) loadedClass))
                    {
                        entry = JsfUtils.getViewConfigResolver()
                                .getViewConfigDescriptor((Class<? extends ViewConfig>) loadedClass);
                    }
                }

                if (entry != null)
                {
                    View.NavigationMode navigationMode = entry.getMetaData(View.class).iterator().next().navigation();

                    navigationCase = new NavigationCase("*",
                            null,
                            null,
                            null,
                            entry.getViewId(),
                            null,
                            View.NavigationMode.REDIRECT.equals(navigationMode),
                            false);
                    this.viewConfigBasedNavigationCaseCache.put(originalOutcome, navigationCase);
                    return navigationCase;
                }
            }
            return ((ConfigurableNavigationHandler) this.wrapped).getNavigationCase(context, action, outcome);
        }
        return null;
    }

    @Override
    public Map<String, Set<NavigationCase>> getNavigationCases()
    {
        Map<String, Set<NavigationCase>> result = null;

        if (this.wrapped instanceof ConfigurableNavigationHandler)
        {
            result = ((ConfigurableNavigationHandler) this.wrapped).getNavigationCases();
        }

        if (result == null)
        {
            result = new HashMap<String, Set<NavigationCase>>();
        }

        if (!this.activated)
        {
            return result;
        }

        return new NavigationCaseMapWrapper(result, this.wrapped);
    }
}
