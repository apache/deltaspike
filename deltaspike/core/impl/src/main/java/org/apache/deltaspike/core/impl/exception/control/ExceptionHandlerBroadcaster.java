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

package org.apache.deltaspike.core.impl.exception.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.api.exception.control.HandlerMethod;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionStackEvent;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionToCatchEvent;
import org.apache.deltaspike.core.api.provider.BeanProvider;

/**
 * Observer of {@link org.apache.deltaspike.core.api.exception.control.event.ExceptionToCatchEvent} events and handler
 * dispatcher. All handlers are invoked from this class. This class is immutable.
 */
@ApplicationScoped
public class ExceptionHandlerBroadcaster
{
    private static final Logger LOG = Logger.getLogger(ExceptionHandlerBroadcaster.class.getName());

    /**
     * Observes the event, finds the correct exception handler(s) and invokes them.
     * 
     * @param exceptionEventEvent
     *            exception to be invoked
     * @param beanManager
     *            active bean manager
     * @throws Throwable
     *             If a handler requests the exception to be re-thrown.
     */
    public void executeHandlers(@Observes @Any ExceptionToCatchEvent exceptionEventEvent,
            final BeanManager beanManager) throws Throwable
    {
        LOG.entering(
                ExceptionHandlerBroadcaster.class.getName(), "executeHandlers", exceptionEventEvent.getException());

        CreationalContext<Object> creationalContext = null;

        Throwable throwException = null;

        final HandlerMethodStorage handlerMethodStorage =
                BeanProvider.getContextualReference(HandlerMethodStorage.class);

        try
        {
            creationalContext = beanManager.createCreationalContext(null);

            final Set<HandlerMethod<?>> processedHandlers = new HashSet<HandlerMethod<?>>();

            final ExceptionStackEvent stack = new ExceptionStackEvent(exceptionEventEvent.getException());

            beanManager.fireEvent(stack); // Allow for modifying the exception stack

        // indentation with 8 for label needed by the current checkstyle rules
        inbound_cause:
            while (stack.getCurrent() != null)
            {
                final List<HandlerMethod<?>> callbackExceptionEvent = new ArrayList<HandlerMethod<?>>(
                        handlerMethodStorage.getHandlersForException(stack.getCurrent().getClass(),
                                beanManager, exceptionEventEvent.getQualifiers(), true));

                for (HandlerMethod<?> handler : callbackExceptionEvent)
                {
                    if (!processedHandlers.contains(handler))
                    {
                        LOG.fine(String.format("Notifying handler %s", handler));

                        @SuppressWarnings("rawtypes")
                        final DefaultExceptionEvent callbackEvent = new DefaultExceptionEvent(stack, true,
                                exceptionEventEvent.isHandled());

                        handler.notify(callbackEvent, beanManager);

                        LOG.fine(String.format("Handler %s returned status %s", handler,
                                callbackEvent.getCurrentExceptionHandlingFlow().name()));

                        if (!callbackEvent.isUnmute())
                        {
                            processedHandlers.add(handler);
                        }

                        switch (callbackEvent.getCurrentExceptionHandlingFlow())
                        {
                            case HANDLED:
                                exceptionEventEvent.setHandled(true);
                                return;
                            case HANDLED_AND_CONTINUE:
                                exceptionEventEvent.setHandled(true);
                                break;
                            case ABORT:
                                return;
                            case SKIP_CAUSE:
                                exceptionEventEvent.setHandled(true);
                                stack.skipCause();
                                continue inbound_cause;
                            case THROW_ORIGINAL:
                                throw exceptionEventEvent.getException();
                            case THROW:
                                throw callbackEvent.getThrowNewException();
                            default:
                                throw new IllegalStateException(
                                        "Unexpected enum type " + callbackEvent.getCurrentExceptionHandlingFlow());
                        }
                    }
                }

                final Collection<HandlerMethod<? extends Throwable>> handlersForException =
                        handlerMethodStorage.getHandlersForException(stack.getCurrent().getClass(),
                                beanManager, exceptionEventEvent.getQualifiers(), false);

                final List<HandlerMethod<? extends Throwable>> handlerMethods =
                        new ArrayList<HandlerMethod<? extends Throwable>>(handlersForException);

                // Reverse these so category handlers are last
                Collections.reverse(handlerMethods);

                for (HandlerMethod<?> handler : handlerMethods)
                {
                    if (!processedHandlers.contains(handler))
                    {
                        LOG.fine(String.format("Notifying handler %s", handler));

                        @SuppressWarnings("rawtypes")
                        final DefaultExceptionEvent depthFirstEvent = new DefaultExceptionEvent(stack, false,
                                exceptionEventEvent.isHandled());
                        handler.notify(depthFirstEvent, beanManager);

                        LOG.fine(String.format("Handler %s returned status %s", handler,
                                depthFirstEvent.getCurrentExceptionHandlingFlow().name()));

                        if (!depthFirstEvent.isUnmute())
                        {
                            processedHandlers.add(handler);
                        }

                        switch (depthFirstEvent.getCurrentExceptionHandlingFlow())
                        {
                            case HANDLED:
                                exceptionEventEvent.setHandled(true);
                                return;
                            case HANDLED_AND_CONTINUE:
                                exceptionEventEvent.setHandled(true);
                                break;
                            case ABORT:
                                return;
                            case SKIP_CAUSE:
                                exceptionEventEvent.setHandled(true);
                                stack.skipCause();
                                continue inbound_cause;
                            case THROW_ORIGINAL:
                                throwException = exceptionEventEvent.getException();
                                break;
                            case THROW:
                                throwException = depthFirstEvent.getThrowNewException();
                                break;
                            default:
                                throw new IllegalStateException(
                                        "Unexpected enum type " + depthFirstEvent.getCurrentExceptionHandlingFlow());
                        }
                    }
                }
                stack.skipCause();
            }

            if (!exceptionEventEvent.isHandled() && throwException == null && !exceptionEventEvent.isOptional())
            {
                LOG.warning(String.format("No handlers found for exception %s", exceptionEventEvent.getException()));
                throw exceptionEventEvent.getException();
            }

            if (throwException != null)
            {
                throw throwException;
            }
        }
        finally
        {
            if (creationalContext != null)
            {
                creationalContext.release();
            }
            LOG.exiting(ExceptionHandlerBroadcaster.class.getName(), "executeHandlers",
                    exceptionEventEvent.getException());
        }
    }
}
