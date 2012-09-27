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
package org.apache.deltaspike.jsf.impl.scope.view;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PreDestroyViewMapEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;

/**
 * This class provides the contexts lifecycle for the
 * new JSF-2 &#064;ViewScoped Context.
 */
public class ViewScopedContext implements Context, SystemEventListener
{
    private static final String COMPONENT_ID_MAP_NAME = "deltaspike.componentIdMap";
    private static final String CREATIONAL_MAP_NAME = "deltaspike.creationalInstanceMap";
    private static final String UNCHECKED = "unchecked";

    private boolean isJsfSubscribed = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Contextual<T> component)
    {
        checkActive();

        if (!isJsfSubscribed)
        {
            FacesContext.getCurrentInstance().getApplication().subscribeToEvent(PreDestroyViewMapEvent.class, this);

            isJsfSubscribed = true;
        }

        Map<String, Object> viewMap = getViewMap();

        @SuppressWarnings(UNCHECKED)
        Map<String, Object> componentIdMap = (Map<String, Object>) viewMap.get(COMPONENT_ID_MAP_NAME);

        if (componentIdMap == null)
        {
            return null;
        }

        @SuppressWarnings(UNCHECKED)
        T instance = (T)componentIdMap.get(((PassivationCapable)component).getId());
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Contextual<T> component, CreationalContext<T> creationalContext)
    {
        if (!(component instanceof PassivationCapable))
        {
            throw new IllegalStateException(component.toString() +
                    " doesn't implement " + PassivationCapable.class.getName());
        }

        checkActive();

        Map<String, Object> viewMap = getViewMap();

        @SuppressWarnings(UNCHECKED)
        Map<String, Object> componentIdMap = (Map<String, Object>) viewMap.get(COMPONENT_ID_MAP_NAME);

        if (componentIdMap == null)
        {
            // TODO we now need to start being carefull with reentrancy...
            componentIdMap = new ConcurrentHashMap<String, Object>();
            viewMap.put(COMPONENT_ID_MAP_NAME, componentIdMap);
        }

        @SuppressWarnings(UNCHECKED)
        T instance = (T) componentIdMap.get(((PassivationCapable)component).getId());
        if (instance != null)
        {
            return instance;
        }

        if (creationalContext == null)
        {
            return null;
        }

        instance = component.create(creationalContext);

        if (instance == null)
        {
            return null;
        }

        Map<String, CreationalContext<?>> creationalContextMap
            = (Map<String, CreationalContext<?>>) viewMap.get(CREATIONAL_MAP_NAME);

        if (creationalContextMap == null)
        {
            creationalContextMap = new ConcurrentHashMap<String, CreationalContext<?>>();
            viewMap.put(CREATIONAL_MAP_NAME, creationalContextMap);
        }

        componentIdMap.put(((PassivationCapable)component).getId(), instance);
        creationalContextMap.put(((PassivationCapable)component).getId(), creationalContext);

        return  instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Annotation> getScope()
    {
        return ViewScoped.class;
    }

    /**
     * The view context is active if a valid ViewRoot could be detected.
     */
    @Override
    public boolean isActive()
    {
        return getViewRoot() != null;
    }

    private void checkActive()
    {
        if (!isActive())
        {
            throw new ContextNotActiveException("WebBeans context with scope annotation " +
                                                "@ViewScoped is not active with respect to the current thread");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isListenerForSource(Object source)
    {
        if (source instanceof UIViewRoot)
        {
            return true;
        }

        return false;
    }

    /**
     * We get PreDestroyViewMapEvent events from the JSF servlet and destroy our contextual
     * instances. This should (theoretically!) also get fired if the webapp closes, so there
     * should be no need to manually track all view scopes and destroy them at a shutdown.
     *
     * @see javax.faces.event.SystemEventListener#processEvent(javax.faces.event.SystemEvent)
     */
    @Override
    public void processEvent(SystemEvent event)
    {
        if (event instanceof PreDestroyViewMapEvent)
        {
            // better use the viewmap we get from the event to prevent concurrent modification problems
            Map<String, Object> viewMap = ((UIViewRoot) event.getSource()).getViewMap();

            Map<String, Object> componentIdMap
                = (Map<String, Object>) viewMap.get(COMPONENT_ID_MAP_NAME);

            Map<String, CreationalContext<?>> creationalContextMap
                = (Map<String, CreationalContext<?>>) viewMap.get(CREATIONAL_MAP_NAME);

            if (componentIdMap != null)
            {
                BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();
                for ( Entry<String, Object> componentEntry : componentIdMap.entrySet())
                {
                    String beanId = componentEntry.getKey();
                    // there is no nice way to explain the Java Compiler that we are handling the same type T,
                    // therefore we need completely drop the type information :(
                    Contextual contextual = beanManager.getPassivationCapableBean(beanId);
                    Object instance = componentEntry.getValue();
                    CreationalContext creational = creationalContextMap.get(beanId);

                    contextual.destroy(instance, creational);
                }
            }
        }
    }


    protected UIViewRoot getViewRoot()
    {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context != null)
        {
            return context.getViewRoot();
        }

        return null;
    }

    protected Map<String, Object> getViewMap()
    {
        UIViewRoot viewRoot = getViewRoot();

        if (viewRoot != null)
        {
            return viewRoot.getViewMap(true);
        }

        return null;
    }
}
