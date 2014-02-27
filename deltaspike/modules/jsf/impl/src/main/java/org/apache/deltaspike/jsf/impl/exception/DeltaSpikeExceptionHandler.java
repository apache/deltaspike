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
package org.apache.deltaspike.jsf.impl.exception;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionToCatchEvent;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;

public class DeltaSpikeExceptionHandler extends ExceptionHandlerWrapper implements Deactivatable
{
    private final ExceptionHandler wrapped;

    private volatile Boolean initialized;
    
    private Annotation exceptionQualifier;
    private boolean isActivated = true;

    public DeltaSpikeExceptionHandler(ExceptionHandler wrapped)
    {
        this.isActivated = ClassDeactivationUtils.isActivated(getClass());
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
        if (isActivated)
        {
            lazyInit();
            
            FacesContext context = FacesContext.getCurrentInstance();

            if (context.getResponseComplete())
            {
                return;
            }

            Iterable<ExceptionQueuedEvent> exceptionQueuedEvents = getUnhandledExceptionQueuedEvents();
            if (exceptionQueuedEvents != null && exceptionQueuedEvents.iterator() != null)
            {
                Iterator<ExceptionQueuedEvent> iterator = exceptionQueuedEvents.iterator();

                BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();

                while (iterator.hasNext())
                {
                    Throwable throwable = iterator.next().getContext().getException();
                    Throwable rootCause = getRootCause(throwable);

                    ExceptionToCatchEvent event = new ExceptionToCatchEvent(rootCause, exceptionQualifier);

                    beanManager.fireEvent(event);

                    if (event.isHandled())
                    {
                        iterator.remove();
                    }

                    // a handle method might redirect and set responseComplete
                    if (context.getResponseComplete())
                    {
                        break;
                    }
                }
            }
        }

        super.handle();
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
        if (this.initialized == null)
        {
            this.exceptionQualifier = AnnotationInstanceProvider.of(
                    BeanProvider.getContextualReference(JsfModuleConfig.class).getExceptionQualifier());

            this.initialized = true;
        }
    }
}
