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
package org.apache.deltaspike.jsf.impl.view;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.jsf.impl.security.SecurityAwareViewHandler;

import jakarta.faces.application.ViewHandler;
import jakarta.faces.application.ViewHandlerWrapper;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

/**
 * Aggregates all {@link ViewHandler} implementations provided by DeltaSpike
 */
public class DeltaSpikeViewHandler extends ViewHandlerWrapper implements Deactivatable
{
    protected final ViewHandler wrapped;

    private volatile Boolean initialized;
    
    private ViewHandler securityAwareViewHandler;

    /**
     * Constructor for wrapping the given {@link ViewHandler}
     *
     * @param wrapped view-handler which should be wrapped
     */
    public DeltaSpikeViewHandler(ViewHandler wrapped)
    {
        this.wrapped = wrapped;
    }

    //allows custom implementations to override the SecurityAwareViewHandler
    protected ViewHandler createSecurityAwareViewHandler()
    {
        return new SecurityAwareViewHandler(this.wrapped);
    }

    @Override
    public UIViewRoot createView(FacesContext facesContext, String viewId)
    {
        lazyInit();
        
        if (this.securityAwareViewHandler == null)
        {
            return this.wrapped.createView(facesContext, viewId);
        }
        return this.securityAwareViewHandler.createView(facesContext, viewId);
    }

    @Override
    public ViewHandler getWrapped()
    {
        return this.wrapped;
    }
    
    private void lazyInit()
    {
        if (this.initialized == null)
        {
            init();
        }
    }

    private synchronized void init()
    {
        // switch into paranoia mode
        if (this.initialized == null)
        {
            if (ClassDeactivationUtils.isActivated(getClass()))
            {
                this.securityAwareViewHandler = createSecurityAwareViewHandler();
            }
            else
            {
                this.securityAwareViewHandler = null;
            }

            this.initialized = true;
        }
    }
}
