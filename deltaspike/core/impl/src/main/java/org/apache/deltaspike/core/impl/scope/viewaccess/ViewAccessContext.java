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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.PassivationCapable;
import org.apache.deltaspike.core.api.scope.ViewAccessScoped;
import org.apache.deltaspike.core.impl.scope.window.WindowContextImpl;
import org.apache.deltaspike.core.impl.scope.window.WindowIdHolder;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualInstanceInfo;
import org.apache.deltaspike.core.util.context.ContextualStorage;

public class ViewAccessContext extends AbstractContext
{
    private final BeanManager beanManager;
    private final WindowContextImpl windowContext;

    private WindowIdHolder windowIdHolder;
    private ViewAccessScopedBeanHolder viewAccessScopedBeanHolder;
    private ViewAccessScopedBeanHistory viewAccessScopedBeanHistory;
    
    public ViewAccessContext(BeanManager beanManager, WindowContextImpl windowContext)
    {
        super(beanManager);

        this.beanManager = beanManager;
        this.windowContext = windowContext;
    }

    public void init(ViewAccessScopedBeanHolder viewAccessScopedBeanHolder, WindowIdHolder windowIdHolder,
            ViewAccessScopedBeanHistory viewAccessScopedBeanHistory)
    {
        this.viewAccessScopedBeanHolder = viewAccessScopedBeanHolder;
        this.windowIdHolder = windowIdHolder;
        this.viewAccessScopedBeanHistory = viewAccessScopedBeanHistory;
    }

    @Override
    public <T> T get(Contextual<T> bean)
    {
        PassivationCapable pc = (PassivationCapable) bean;
        viewAccessScopedBeanHistory.getAccessedBeans().add(pc.getId());

        return super.get(bean);
    }

    @Override
    public <T> T get(Contextual<T> bean, CreationalContext<T> creationalContext)
    {
        PassivationCapable pc = (PassivationCapable) bean;
        viewAccessScopedBeanHistory.getAccessedBeans().add(pc.getId());

        return super.get(bean, creationalContext);
    }
    
    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist)
    {
        String windowId = getCurrentWindowId();
        if (windowId == null)
        {
            throw new ContextNotActiveException("WindowContext: no windowId set for the current Thread yet!");
        }
        
        return this.viewAccessScopedBeanHolder.getContextualStorage(this.beanManager, windowId, createIfNotExist);
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
    
    public String getCurrentWindowId()
    {
        return windowIdHolder.getWindowId();
    }

    public void onRenderingFinished(String view)
    {
        if (!view.equals(viewAccessScopedBeanHistory.getLastView()))
        {
            viewAccessScopedBeanHistory.setLastView(view);
            
            destroyExpiredBeans();
            
            // clear list from last request
            List<String> lastAccessedBeans = viewAccessScopedBeanHistory.getLastAccessedBeans();
            lastAccessedBeans.clear();
            
            // move used beans from this request to last request
            viewAccessScopedBeanHistory.setLastAccessedBeans(viewAccessScopedBeanHistory.getAccessedBeans());
            viewAccessScopedBeanHistory.setAccessedBeans(lastAccessedBeans);
        }
    }
    
    private void destroyExpiredBeans()
    {
        List<String> usedBeans = new ArrayList<String>();
        usedBeans.addAll(viewAccessScopedBeanHistory.getAccessedBeans());
        usedBeans.addAll(viewAccessScopedBeanHistory.getLastAccessedBeans());

        ContextualStorage storage =
                viewAccessScopedBeanHolder.getContextualStorage(beanManager, getCurrentWindowId(), false);
        if (storage != null)
        {
            for (Map.Entry<Object, ContextualInstanceInfo<?>> storageEntry : storage.getStorage().entrySet())
            {
                if (!usedBeans.contains((String) storageEntry.getKey()))
                {
                    Contextual bean = storage.getBean(storageEntry.getKey());
                    AbstractContext.destroyBean(bean, storageEntry.getValue());
                    storage.getStorage().remove(storageEntry.getKey()); //ok due to ConcurrentHashMap
                    break;
                }
            }
        }
    }
}
