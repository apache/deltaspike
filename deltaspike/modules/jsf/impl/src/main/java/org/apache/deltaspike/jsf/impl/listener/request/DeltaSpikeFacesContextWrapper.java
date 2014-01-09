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

import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;
import org.apache.deltaspike.jsf.impl.config.view.DefaultErrorViewAwareExceptionHandlerWrapper;
import org.apache.deltaspike.jsf.impl.injection.InjectionAwareApplicationWrapper;
import org.apache.deltaspike.jsf.impl.message.FacesMessageEntry;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextWrapper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindow;

class DeltaSpikeFacesContextWrapper extends FacesContextWrapper
{
    private final FacesContext wrappedFacesContext;

    private BeforeAfterJsfRequestBroadcaster beforeAfterJsfRequestBroadcaster;

    private boolean defaultErrorViewExceptionHandlerActivated;

    private ExternalContext wrappedExternalContext;

    private JsfModuleConfig jsfModuleConfig;

    private volatile Boolean initialized;

    DeltaSpikeFacesContextWrapper(FacesContext wrappedFacesContext, ClientWindow clientWindow)
    {
        this.wrappedFacesContext = wrappedFacesContext;

        if (ClassDeactivationUtils.isActivated(DeltaSpikeExternalContextWrapper.class))
        {
            this.wrappedExternalContext =
                    new DeltaSpikeExternalContextWrapper(wrappedFacesContext.getExternalContext(), clientWindow);
        }
        else
        {
            this.wrappedExternalContext = wrappedFacesContext.getExternalContext();
        }

        setCurrentInstance(this);
    }

    /**
     * Broadcasts the {@link org.apache.deltaspike.jsf.api.listener.request.AfterJsfRequest} event
     * {@inheritDoc}
     */
    @Override
    public void release()
    {
        if (!this.wrappedFacesContext.getApplication().getResourceHandler().isResourceRequest(this.wrappedFacesContext))
        {
            broadcastAfterJsfRequestEvent();
        }

        wrappedFacesContext.release();
    }

    @Override
    public ExceptionHandler getExceptionHandler()
    {
        lazyInit();

        ExceptionHandler exceptionHandler = this.wrappedFacesContext.getExceptionHandler();

        if (this.defaultErrorViewExceptionHandlerActivated)
        {
            exceptionHandler = new DefaultErrorViewAwareExceptionHandlerWrapper(exceptionHandler);
        }
        return exceptionHandler;
    }

    private void broadcastAfterJsfRequestEvent()
    {
        lazyInit();
        if (this.beforeAfterJsfRequestBroadcaster != null)
        {
            this.beforeAfterJsfRequestBroadcaster.broadcastAfterJsfRequestEvent(this);
        }
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
        if (initialized == null)
        {
            if (ClassDeactivationUtils.isActivated(BeforeAfterJsfRequestBroadcaster.class))
            {
                this.beforeAfterJsfRequestBroadcaster =
                        BeanProvider.getContextualReference(BeforeAfterJsfRequestBroadcaster.class, true);
            }

            ViewConfigResolver viewConfigResolver = BeanProvider.getContextualReference(ViewConfigResolver.class);

            //deactivate it, if there is no default-error-view available
            this.defaultErrorViewExceptionHandlerActivated =
                    viewConfigResolver.getDefaultErrorViewConfigDescriptor() != null &&
                            ClassDeactivationUtils.isActivated(DefaultErrorViewAwareExceptionHandlerWrapper.class);
        }
    }

    /**
     * Adds the {@link FacesMessage} also to a request scoped list to allow to preserve them later on
     * (in case of redirects)
     *
     * {@inheritDoc}
     */
    @Override
    public void addMessage(String componentId, FacesMessage facesMessage)
    {
        this.wrappedFacesContext.addMessage(componentId, facesMessage);

        //don't store it directly in the window context - it would trigger a too early restore (in some cases)
        Map<String, Object> requestMap = getExternalContext().getRequestMap();

        @SuppressWarnings({ "unchecked" })
        List<FacesMessageEntry> facesMessageEntryList =
                (List<FacesMessageEntry>)requestMap.get(FacesMessageEntry.class.getName());

        if (facesMessageEntryList == null)
        {
            facesMessageEntryList = new CopyOnWriteArrayList<FacesMessageEntry>();
            requestMap.put(FacesMessageEntry.class.getName(), facesMessageEntryList);
        }

        facesMessageEntryList.add(new FacesMessageEntry(componentId, facesMessage));
    }

    @Override
    public ExternalContext getExternalContext()
    {
        return this.wrappedExternalContext;
    }

    @Override
    public Application getApplication()
    {
        if (this.jsfModuleConfig == null)
        {
            this.jsfModuleConfig = BeanProvider.getContextualReference(JsfModuleConfig.class);
        }

        return new InjectionAwareApplicationWrapper(this.wrappedFacesContext.getApplication(), this.jsfModuleConfig);
    }

    @Override
    public FacesContext getWrapped()
    {
        return this.wrappedFacesContext;
    }
}
