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
package org.apache.deltaspike.jsf.impl.config.view;

import org.apache.deltaspike.core.api.config.view.DefaultErrorView;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.config.view.navigation.ViewNavigationHandler;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.api.projectstage.TestStage;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ProjectStageProducer;

import javax.enterprise.context.ContextNotActiveException;
import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import java.util.Iterator;

public class DefaultErrorViewAwareExceptionHandlerWrapper extends ExceptionHandlerWrapper implements Deactivatable
{
    private ExceptionHandler wrapped;

    private volatile ViewNavigationHandler viewNavigationHandler;

    /**
     * Constructor used by proxy libs
     */
    protected DefaultErrorViewAwareExceptionHandlerWrapper()
    {
    }

    public DefaultErrorViewAwareExceptionHandlerWrapper(ExceptionHandler wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public void handle() throws FacesException
    {
        lazyInit();
        Iterator<ExceptionQueuedEvent> exceptionQueuedEventIterator = getUnhandledExceptionQueuedEvents().iterator();

        while (exceptionQueuedEventIterator.hasNext())
        {
            ExceptionQueuedEventContext exceptionQueuedEventContext =
                    (ExceptionQueuedEventContext) exceptionQueuedEventIterator.next().getSource();

            @SuppressWarnings({ "ThrowableResultOfMethodCallIgnored" })
            Throwable throwable = exceptionQueuedEventContext.getException();

            String viewId = null;

            if (!isExceptionToHandle(throwable))
            {
                continue;
            }

            FacesContext facesContext = exceptionQueuedEventContext.getContext();
            Flash flash = facesContext.getExternalContext().getFlash();

            if (throwable instanceof ViewExpiredException)
            {
                viewId = ((ViewExpiredException) throwable).getViewId();
            }
            else if (throwable instanceof ContextNotActiveException)
            {
                //the error page uses a cdi scope which isn't active as well
                //(it's recorded below - see flash.put(throwable.getClass().getName(), throwable);)
                if (flash.containsKey(ContextNotActiveException.class.getName()))
                {
                    //TODO show it in case of project-stage development
                    break;
                }

                if (facesContext.getViewRoot() != null)
                {
                    viewId = facesContext.getViewRoot().getViewId();
                }
                else
                {
                    viewId = BeanProvider.getContextualReference(ViewConfigResolver.class)
                            //has to return a value otherwise this handler wouldn't be active
                            .getDefaultErrorViewConfigDescriptor().getViewId();
                }
            }

            if (viewId != null)
            {
                UIViewRoot uiViewRoot = facesContext.getApplication().getViewHandler().createView(facesContext, viewId);

                if (uiViewRoot == null)
                {
                    continue;
                }

                if (facesContext.isProjectStage(javax.faces.application.ProjectStage.Development) ||
                        ProjectStageProducer.getInstance().getProjectStage() == ProjectStage.Development ||
                        ProjectStageProducer.getInstance().getProjectStage() instanceof TestStage)
                {
                    throwable.printStackTrace();
                }

                facesContext.setViewRoot(uiViewRoot);
                exceptionQueuedEventIterator.remove();

                //record the current exception -> to check it at the next call or to use it on the error-page
                flash.put(throwable.getClass().getName(), throwable);
                flash.keep(throwable.getClass().getName());

                this.viewNavigationHandler.navigateTo(DefaultErrorView.class);

                break;
            }
        }

        this.wrapped.handle();
    }

    //TODO it should be possible to configure the exceptions we handle
    //e.g. @View could specify which exception/s a page
    // - can recover from (-> redirect in case of a POST to refresh the state + optional message)
    // - can't recover from (-> redirect to the default-error page)
    protected boolean isExceptionToHandle(Throwable throwable)
    {
        return throwable instanceof ViewExpiredException ||
                throwable instanceof ContextNotActiveException;
    }

    private void lazyInit()
    {
        if (this.viewNavigationHandler == null)
        {
            this.viewNavigationHandler = BeanProvider.getContextualReference(ViewNavigationHandler.class);
        }
    }

    @Override
    public ExceptionHandler getWrapped()
    {
        lazyInit();
        return wrapped;
    }
}
