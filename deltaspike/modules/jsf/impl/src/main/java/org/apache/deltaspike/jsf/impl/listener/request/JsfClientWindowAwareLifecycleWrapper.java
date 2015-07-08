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
package org.apache.deltaspike.jsf.impl.listener.request;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.jsf.impl.scope.window.ClientWindowAdapter;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindow;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig;

import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleWrapper;
import java.lang.reflect.Field;
import javax.faces.FacesException;

//ATTENTION: don't rename/move this class as long as we need the workaround in impl-ee6
//(further details are available at: DELTASPIKE-655 and DELTASPIKE-659)

@SuppressWarnings("UnusedDeclaration")
public class JsfClientWindowAwareLifecycleWrapper extends LifecycleWrapper
{
    private final Lifecycle wrapped;

    private volatile Boolean initialized;
    private ClientWindowConfig clientWindowConfig;

    public JsfClientWindowAwareLifecycleWrapper(Lifecycle wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public Lifecycle getWrapped()
    {
        return wrapped;
    }

    @Override
    public void attachWindow(FacesContext facesContext)
    {
        lazyInit();

        boolean delegateWindowHandling = ClientWindowConfig.ClientWindowRenderMode.DELEGATED.equals(
                clientWindowConfig.getClientWindowRenderMode(facesContext));
        
        if (delegateWindowHandling)
        {
            try
            {
                //the first wrapper is always DeltaSpikeLifecycleWrapper which can't extend from LifecycleWrapper
                Lifecycle externalWrapper = ((DeltaSpikeLifecycleWrapper)this.wrapped).getWrapped();
                delegateAttachWindow(facesContext, externalWrapper);
            }
            catch (Exception e)
            {
                try
                {
                    attachWindowOnUnwrappedInstance(facesContext, this.wrapped);
                }
                catch (Exception e1)
                {
                    throw ExceptionUtils.throwAsRuntimeException(e);
                }
            }
        }
        else
        {
            ClientWindow clientWindow = BeanProvider.getContextualReference(ClientWindow.class);
            //trigger init - might lead to a redirect -> response-complete
            String windowId = clientWindow.getWindowId(facesContext);

            if (!facesContext.getResponseComplete() && !"default".equals(windowId))
            {
                facesContext.getExternalContext().setClientWindow(new ClientWindowAdapter(clientWindow));
            }
        }
    }

    private void attachWindowOnUnwrappedInstance(FacesContext facesContext, Lifecycle wrapped) throws Exception
    {
        Lifecycle wrappedLifecycle = null;

        if (wrapped instanceof LifecycleWrapper)
        {
            wrappedLifecycle = ((LifecycleWrapper)wrapped).getWrapped();
        }

        //needed to support some libs which don't use LifecycleWrapper, because it's a jsf 2.2+ api
        if (wrappedLifecycle == null)
        {
            for (Field field : wrapped.getClass().getDeclaredFields())
            {
                if (Lifecycle.class.isAssignableFrom(field.getType()))
                {
                    if (!field.isAccessible())
                    {
                        field.setAccessible(true);
                    }
                    wrappedLifecycle = (Lifecycle)field.get(wrapped);
                    break;
                }
            }
        }

        if (wrappedLifecycle != null)
        {
            try
            {
                delegateAttachWindow(facesContext, wrappedLifecycle);
            }
            catch (Exception e)
            {
                attachWindowOnUnwrappedInstance(facesContext, wrappedLifecycle);
            }
        }
    }

    private static void delegateAttachWindow(FacesContext facesContext, Lifecycle lifecycle) throws Exception
    {
        //if there is an external wrapper (e.g. in case of other libs), we have to check
        //the version of javax.faces.lifecycle.Lifecycle (>= or < v2.2)
        //without the check and an old lib (in the classpath) #attachWindow would get ignored without exception
        if (lifecycle instanceof LifecycleWrapper /*autom. provides #attachWindow*/ ||
                lifecycle.getClass().getDeclaredMethod("attachWindow", FacesContext.class) != null)
        {
            lifecycle.attachWindow(facesContext);
        }
    }

    @Override
    public void render(FacesContext facesContext) throws FacesException
    {
        lazyInit();

        // prevent jfwid rendering
        boolean delegateWindowHandling = ClientWindowConfig.ClientWindowRenderMode.DELEGATED.equals(
                clientWindowConfig.getClientWindowRenderMode(facesContext));
        if (!delegateWindowHandling && facesContext.getExternalContext().getClientWindow() != null)
        {
            facesContext.getExternalContext().getClientWindow().disableClientWindowRenderMode(facesContext);
        }
        
        super.render(facesContext);
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
            this.clientWindowConfig = BeanProvider.getContextualReference(ClientWindowConfig.class);

            this.initialized = true;
        }
    }
}
