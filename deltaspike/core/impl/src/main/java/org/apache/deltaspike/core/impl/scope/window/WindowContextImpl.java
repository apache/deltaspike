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
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanManager;

import java.lang.annotation.Annotation;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;

/**
 * CDI Context to handle &#064;{@link WindowScoped} beans.
 * This also implements the interface to control the id of
 * the currently active 'window' (e.g. a web browser tab).
 */
@Typed()
public class WindowContextImpl extends AbstractContext implements WindowContext
{
    /**
     * Holds the currently active windowId of each Request
     */
    private WindowIdHolder windowIdHolder;

    /**
     * Contains the stored WindowScoped contextual instances.
     */
    private WindowBeanHolder windowBeanHolder;

    /**
     * needed for serialisation and passivationId
     */
    private BeanManager beanManager;


    public WindowContextImpl(BeanManager beanManager)
    {
        super(beanManager);

        this.beanManager = beanManager;
    }

    /**
     * We need to pass the session scoped windowbean holder and the
     * requestscoped windowIdHolder in a later phase because
     * getBeans is only allowed from AfterDeploymentValidation onwards.
     */
    public void init(WindowBeanHolder windowBeanHolder, WindowIdHolder windowIdHolder)
    {
        this.windowBeanHolder = windowBeanHolder;
        this.windowIdHolder = windowIdHolder;
    }


    @Override
    public void activateWindow(String windowId)
    {
        windowIdHolder.setWindowId(windowId);
    }

    @Override
    public String getCurrentWindowId()
    {
        return windowIdHolder.getWindowId();
    }

    @Override
    public boolean closeWindow(String windowId)
    {
        if (windowId == null)
        {
            return false;
        }

        ContextualStorage windowStorage = windowBeanHolder.getStorageMap().remove(windowId);

        if (windowStorage != null)
        {
            if (windowId.equals(this.windowIdHolder.getWindowId()))
            {
                this.windowIdHolder.setWindowId(null);
            }
            AbstractContext.destroyAllActive(windowStorage);
        }

        return windowStorage != null;
    }

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist)
    {
        String windowId = getCurrentWindowId();
        if (windowId == null)
        {
            throw new ContextNotActiveException("WindowContext: no windowId set for the current Thread yet!");
        }

        return windowBeanHolder.getContextualStorage(beanManager, windowId, createIfNotExist);
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
