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
package org.apache.deltaspike.jsf.impl.exception.control;

import java.util.Iterator;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.SystemEvent;

import org.apache.deltaspike.core.api.config.view.DefaultErrorView;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.jsf.impl.util.JsfUtils;
import org.apache.deltaspike.jsf.impl.util.SecurityUtils;
import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.apache.deltaspike.security.api.authorization.ErrorViewAwareAccessDeniedException;

public class BridgeExceptionHandlerWrapper extends ExceptionHandlerWrapper implements Deactivatable
{
    private final ExceptionHandler wrapped;

    public BridgeExceptionHandlerWrapper(ExceptionHandler wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped()
    {
        return wrapped;
    }

    @Override
    public void handle() throws FacesException
    {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context == null || context.getResponseComplete())
        {
            return;
        }

        Iterable<ExceptionQueuedEvent> exceptionQueuedEvents = getUnhandledExceptionQueuedEvents();
        if (exceptionQueuedEvents != null && exceptionQueuedEvents.iterator() != null)
        {
            Iterator<ExceptionQueuedEvent> iterator = exceptionQueuedEvents.iterator();

            while (iterator.hasNext())
            {
                Throwable throwable = iterator.next().getContext().getException();
                Throwable rootCause = getRootCause(throwable);

                if (rootCause instanceof AccessDeniedException)
                {
                    processAccessDeniedException(rootCause);
                    iterator.remove();
                    continue;
                }

                // a handle method might redirect and set responseComplete
                if (context.getResponseComplete())
                {
                    break;
                }
            }
        }

        super.handle();
    }

    @Override
    public Throwable getRootCause(Throwable throwable)
    {
        return JsfUtils.getRootCause(throwable);
    }

    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException
    {
        //handle exceptions which occur in a phase-listener (beforePhase) for PhaseId.RENDER_RESPONSE
        //needed because #handle gets called too late in this case
        if (event instanceof ExceptionQueuedEvent)
        {
            ExceptionQueuedEvent exceptionQueuedEvent = (ExceptionQueuedEvent)event;
            FacesContext facesContext = exceptionQueuedEvent.getContext().getContext();

            if (facesContext.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE &&
                exceptionQueuedEvent.getContext().inBeforePhase())
            {
                Throwable exception = getRootCause(exceptionQueuedEvent.getContext().getException());

                if (exception instanceof AccessDeniedException)
                {
                    processAccessDeniedException(exception);
                }
            }
        }
        super.processEvent(event);
    }

    private void processAccessDeniedException(Throwable throwable)
    {
        if (throwable instanceof ErrorViewAwareAccessDeniedException)
        {
            SecurityUtils.handleSecurityViolationWithoutNavigation((AccessDeniedException) throwable);
        }
        else
        {
            ErrorViewAwareAccessDeniedException securityException =
                new ErrorViewAwareAccessDeniedException(
                    ((AccessDeniedException)throwable).getViolations(), DefaultErrorView.class);
            SecurityUtils.handleSecurityViolationWithoutNavigation(securityException);
        }
    }
}
