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

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PreDestroyViewMapEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import java.lang.annotation.Annotation;
import java.util.Map;

import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;

/**
 * This class provides the contexts lifecycle for the
 * new JSF-2 &#064;ViewScoped Context.
 */
public class ViewScopedContext extends AbstractContext implements SystemEventListener
{
    private static final String CONTEXTUAL_MAP_NAME = "deltaspike.contextualInstanceMap";

    private boolean isJsfSubscribed = false;

    private BeanManager beanManager;

    public ViewScopedContext(BeanManager beanManager)
    {
        super(beanManager);
        this.beanManager = beanManager;
    }

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExists)
    {
        Map<String, Object> viewMap = getViewMap();
        ContextualStorage storage = (ContextualStorage) viewMap.get(CONTEXTUAL_MAP_NAME);

        if (storage == null && createIfNotExists)
        {
            storage = new ContextualStorage(beanManager, false, isPassivatingScope());
            viewMap.put(CONTEXTUAL_MAP_NAME, storage);
        }

        return storage;
    }

    @Override
    public <T> T get(Contextual<T> bean)
    {
        subscribeToJsf();

        return super.get(bean);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Contextual<T> bean, CreationalContext<T> creationalContext)
    {
        subscribeToJsf();

        return super.get(bean, creationalContext);
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

    private void subscribeToJsf()
    {
        if (!isJsfSubscribed)
        {
            FacesContext.getCurrentInstance().getApplication().subscribeToEvent(PreDestroyViewMapEvent.class, this);

            isJsfSubscribed = true;
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
            destroyAllActive();
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
