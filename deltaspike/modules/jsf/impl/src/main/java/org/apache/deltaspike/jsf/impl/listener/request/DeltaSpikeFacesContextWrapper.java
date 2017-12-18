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

import java.lang.annotation.Annotation;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.projectstage.TestStage;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;
import org.apache.deltaspike.jsf.impl.config.view.DefaultErrorViewAwareExceptionHandlerWrapper;
import org.apache.deltaspike.jsf.impl.injection.InjectionAwareApplicationWrapper;
import org.apache.deltaspike.jsf.impl.message.FacesMessageEntry;

import javax.enterprise.inject.spi.BeanManager;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.ProjectStage;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextWrapper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.jsf.impl.exception.control.BridgeExceptionHandlerWrapper;

import org.apache.deltaspike.jsf.impl.navigation.NavigationHandlerAwareApplication;
import org.apache.deltaspike.jsf.impl.security.SecurityAwareViewHandler;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindow;

class DeltaSpikeFacesContextWrapper extends FacesContextWrapper
{
    private final FacesContext wrappedFacesContext;

    private BeanManager beanManager;

    private JsfRequestBroadcaster jsfRequestBroadcaster;

    private boolean defaultErrorViewExceptionHandlerActivated;

    private boolean bridgeExceptionHandlerActivated;
    private Annotation bridgeExceptionQualifier;

    private ExternalContext wrappedExternalContext;

    private JsfModuleConfig jsfModuleConfig;

    private volatile Boolean initialized;
    private volatile Boolean isNavigationAwareApplicationWrapperActivated;

    private boolean preDestroyViewMapEventFilterMode;
    private ProjectStage projectStage;

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
     * Broadcasts the {@link org.apache.deltaspike.core.api.lifecycle.Destroyed} event
     * {@inheritDoc}
     */
    @Override
    public void release()
    {
        // try/finally shouldn't be required...
        try
        {
            if (!this.wrappedFacesContext.getApplication().getResourceHandler()
                    .isResourceRequest(this.wrappedFacesContext))
            {
                broadcastDestroyedJsfRequestEvent();
            }
        }
        finally
        {
            super.release();
        }
    }
    
    @Override
    public ExceptionHandler getExceptionHandler()
    {
        lazyInit();

        ExceptionHandler exceptionHandler = this.wrappedFacesContext.getExceptionHandler();

        if (this.bridgeExceptionHandlerActivated)
        {
            exceptionHandler = new BridgeExceptionHandlerWrapper(
                exceptionHandler, this.beanManager, this.bridgeExceptionQualifier);
        }
        
        if (this.defaultErrorViewExceptionHandlerActivated)
        {
            exceptionHandler = new DefaultErrorViewAwareExceptionHandlerWrapper(exceptionHandler);
        }

        return exceptionHandler;
    }

    private void broadcastDestroyedJsfRequestEvent()
    {
        lazyInit();
        if (this.jsfRequestBroadcaster != null)
        {
            this.jsfRequestBroadcaster.broadcastDestroyedJsfRequestEvent(this);
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
        if (this.initialized == null)
        {
            this.beanManager = BeanManagerProvider.getInstance().getBeanManager();
            this.jsfModuleConfig = BeanProvider.getContextualReference(this.beanManager, JsfModuleConfig.class, false);

            if (ClassDeactivationUtils.isActivated(JsfRequestBroadcaster.class))
            {
                this.jsfRequestBroadcaster =
                        BeanProvider.getContextualReference(JsfRequestBroadcaster.class, true);
            }

            ViewConfigResolver viewConfigResolver = BeanProvider.getContextualReference(ViewConfigResolver.class);

            //deactivate it, if there is no default-error-view available
            this.defaultErrorViewExceptionHandlerActivated =
                    viewConfigResolver.getDefaultErrorViewConfigDescriptor() != null &&
                            ClassDeactivationUtils.isActivated(DefaultErrorViewAwareExceptionHandlerWrapper.class);
            
            this.bridgeExceptionHandlerActivated =
                    ClassDeactivationUtils.isActivated(BridgeExceptionHandlerWrapper.class);
            
            this.bridgeExceptionQualifier = AnnotationInstanceProvider.of(jsfModuleConfig.getExceptionQualifier());

            this.preDestroyViewMapEventFilterMode = ClassDeactivationUtils.isActivated(SecurityAwareViewHandler.class);
            this.isNavigationAwareApplicationWrapperActivated =
                ClassDeactivationUtils.isActivated(NavigationHandlerAwareApplication.class);
            org.apache.deltaspike.core.api.projectstage.ProjectStage dsProjectStage =
                ProjectStageProducer.getInstance().getProjectStage();

            for (ProjectStage ps : ProjectStage.values())
            {
                if (ps.name().equals(dsProjectStage.getClass().getSimpleName()))
                {
                    this.projectStage = ps;
                    break;
                }
            }

            if (this.projectStage == null && dsProjectStage instanceof TestStage)
            {
                this.projectStage = ProjectStage.Development;
            }

            if (this.projectStage == ProjectStage.Production)
            {
                this.projectStage = null; //reset it to force the delegation to the default handling
            }

            this.initialized = true;
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
        lazyInit();

        Application wrappedApplication = this.wrappedFacesContext.getApplication();
        if (this.isNavigationAwareApplicationWrapperActivated)
        {
            wrappedApplication = new NavigationHandlerAwareApplication(wrappedApplication);
        }
        return new InjectionAwareApplicationWrapper(
            wrappedApplication, this.jsfModuleConfig, this.preDestroyViewMapEventFilterMode, this.projectStage);
    }

    @Override
    public FacesContext getWrapped()
    {
        return this.wrappedFacesContext;
    }
}
