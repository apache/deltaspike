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
package org.apache.deltaspike.core.impl.scope.window;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;

/**
 * Context to handle &#064;{@link WindowScoped} beans.
 */
public class DefaultWindowContext extends AbstractContext implements WindowContext
{
    /**
     * all the {@link WindowContext}s which are active in this very Session.
     */
    private Map<String, WindowContext> windowContexts = new ConcurrentHashMap<String, WindowContext>();

    @Inject
    private WindowIdHolder windowIdHolder;

    @Inject
    private WindowBeanHolder windowBeanHolder;

    private BeanManager beanManager;


    public DefaultWindowContext(BeanManager beanManager)
    {
        super(beanManager);

        this.beanManager = beanManager;
    }

    @Override
    public void activateWindowContext(String windowId)
    {
        windowIdHolder.setWindowId(windowId);
    }

    @Override
    public String getCurrentWindowId()
    {
        return windowIdHolder.getWindowId();
    }

    @Override
    public boolean closeCurrentWindowContext()
    {
        String windowId = windowIdHolder.getWindowId();
        if (windowId == null)
        {
            return false;
        }

        WindowContext windowContext = windowContexts.get(windowId);
        if (windowContext == null)
        {
            return false;
        }

        return true;
    }

    @Override
    public synchronized void destroy()
    {
        // we replace the old windowBeanHolder beans with a new storage Map
        // an afterwards destroy the old Beans without having to care about any syncs.
        Map<String, ContextualStorage> oldWindowContextStorages = windowBeanHolder.forceNewStorage();

        for (ContextualStorage contextualStorage : oldWindowContextStorages.values())
        {
            destroyAllActive(contextualStorage);
        }
    }

    @Override
    protected ContextualStorage getContextualStorage(boolean createIfNotExist)
    {
        String windowId = getCurrentWindowId();
        if (windowId == null)
        {
            throw new ContextNotActiveException("WindowContext: no windowId set for the current Thread yet!");
        }

        return windowBeanHolder.getContextualStorage(beanManager, windowId);
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return WindowScoped.class;
    }

    /**
     * The WindowContext is active once a current windowId is set for the current Thread.
     * @return
     */
    @Override
    public boolean isActive()
    {
        String windowId = getCurrentWindowId();
        return windowId != null;
    }
}
