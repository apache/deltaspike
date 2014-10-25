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
package org.apache.deltaspike.core.impl.scope.viewaccess;

import org.apache.deltaspike.core.api.scope.ViewAccessScoped;
import org.apache.deltaspike.core.impl.scope.window.WindowContextImpl;
import org.apache.deltaspike.core.spi.scope.viewaccess.ViewAccessContextManager;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualInstanceInfo;
import org.apache.deltaspike.core.util.context.ContextualStorage;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.PassivationCapable;
import java.lang.annotation.Annotation;
import java.util.Map;

@Typed()
public class ViewAccessContext extends AbstractContext implements ViewAccessContextManager
{
    private static final String KEY = "VAS"; //TODO re-visit key (e.g. view-id instead of using one big storage)

    private final BeanManager beanManager;
    private final WindowContextImpl windowContext;

    private ViewAccessBeanHolder viewAccessBeanHolder;
    private ViewAccessBeanAccessHistory viewAccessBeanAccessHistory;
    private ViewAccessViewHistory viewAccessViewHistory;
    
    public ViewAccessContext(BeanManager beanManager, WindowContextImpl windowContext)
    {
        super(beanManager);

        this.beanManager = beanManager;
        this.windowContext = windowContext;
    }

    public void init(ViewAccessBeanHolder viewAccessBeanHolder,
            ViewAccessBeanAccessHistory viewAccessBeanAccessHistory,
            ViewAccessViewHistory viewAccessViewHistory)
    {
        this.viewAccessBeanHolder = viewAccessBeanHolder;
        this.viewAccessBeanAccessHistory = viewAccessBeanAccessHistory;
        this.viewAccessViewHistory = viewAccessViewHistory;
    }

    @Override
    public <T> T get(Contextual<T> bean)
    {
        try
        {
            return super.get(bean);
        }
        finally
        {
            if (bean instanceof PassivationCapable)
            {
                PassivationCapable pc = (PassivationCapable) bean;
                viewAccessBeanAccessHistory.getAccessedBeans().add(pc.getId());
            }
        }
    }

    @Override
    public <T> T get(Contextual<T> bean, CreationalContext<T> creationalContext)
    {
        try
        {
            return super.get(bean, creationalContext);
        }
        finally
        {
            if (bean instanceof PassivationCapable)
            {
                PassivationCapable pc = (PassivationCapable) bean;
                viewAccessBeanAccessHistory.getAccessedBeans().add(pc.getId());
            }
        }
    }
    
    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist)
    {
        return this.viewAccessBeanHolder.getContextualStorage(this.beanManager, KEY, createIfNotExist);
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return ViewAccessScoped.class;
    }

    @Override
    public boolean isActive()
    {
        return this.windowContext.isActive(); //autom. active once a window is active
    }

    public void onProcessingViewFinished(String view)
    {
        close(view, false);
    }

    public void close(String view, boolean force)
    {
        // ignore if WindowContext isn't active - our ViewAccessViewHistory is WindowScoped
        if (!windowContext.isActive())
        {
            return;
        }

        // destroy beans only if the view has been changed
        if (force || !view.equals(viewAccessViewHistory.getLastView()))
        {
            viewAccessViewHistory.setLastView(view);
            
            destroyExpiredBeans(force);
        }
        
        // clear history after each rendering process
        viewAccessBeanAccessHistory.getAccessedBeans().clear();
    }
    
    private void destroyExpiredBeans(boolean force)
    {
        ContextualStorage storage = viewAccessBeanHolder.getContextualStorage(beanManager, KEY, false);
        if (storage != null)
        {
            for (Map.Entry<Object, ContextualInstanceInfo<?>> storageEntry : storage.getStorage().entrySet())
            {
                if (force || !viewAccessBeanAccessHistory.getAccessedBeans().contains((String) storageEntry.getKey()))
                {
                    Contextual bean = storage.getBean(storageEntry.getKey());
                    AbstractContext.destroyBean(bean, storageEntry.getValue());
                    storage.getStorage().remove(storageEntry.getKey()); //ok due to ConcurrentHashMap
                }
            }
        }
    }

    @Override
    public void close()
    {
        close(null, true);
    }
}
