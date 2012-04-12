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

import org.apache.deltaspike.core.api.exception.control.ExceptionStack;
import org.apache.deltaspike.core.api.exception.control.ExceptionToCatch;
import org.apache.deltaspike.core.api.exception.control.HandlerMethod;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.BeanManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Observer of {@link ExceptionToCatch} events and handler dispatcher. All handlers are invoked from this class.  This
 * class is immutable.
 */
@ApplicationScoped
@SuppressWarnings("UnusedDeclaration")
public class ExceptionHandlerDispatch
{
    private static final Logger LOG = Logger.getLogger(ExceptionHandlerDispatch.class.getName());

    /**
     * Observes the event, finds the correct exception handler(s) and invokes them.
     *
     * @param exceptionEvent exception to be invoked
     * @param beanManager    active bean manager
     * @throws Throwable If a handler requests the exception to be re-thrown.
     */
    @SuppressWarnings({"unchecked", "MethodWithMultipleLoops", "ThrowableResultOfMethodCallIgnored"})
    public void executeHandlers(@Observes @Any ExceptionToCatch exceptionEvent,
                                final BeanManager beanManager) throws Throwable
    {
        LOG.entering(ExceptionHandlerDispatch.class.getName(), "executeHandlers", exceptionEvent.getException());

        CreationalContext<Object> creationalContext = null;

        Throwable throwException = null;

        final HandlerMethodStorage handlerMethodStorage =
                BeanProvider.getContextualReference(HandlerMethodStorage.class);

        try
        {
            creationalContext = beanManager.createCreationalContext(null);

            final Set<HandlerMethod<?>> processedHandlers = new HashSet<HandlerMethod<?>>();

            final ExceptionStack stack = new ExceptionStack(exceptionEvent.getException());

            beanManager.fireEvent(stack); // Allow for modifying the exception stack

            inbound_cause:
            //indentation needed by the current checkstyle rules
            while (stack.getCurrent() != null)
            {
                final List<HandlerMethod<?>> breadthFirstHandlerMethods = new ArrayList<HandlerMethod<?>>(
                        handlerMethodStorage.getHandlersForException(stack.getCurrent().getClass(),
                                beanManager, exceptionEvent.getQualifiers(), true));

                for (HandlerMethod<?> handler : breadthFirstHandlerMethods)
                {
                    if (!processedHandlers.contains(handler))
                    {
                        LOG.fine(String.format("Notifying handler %s", handler));

                        @SuppressWarnings("rawtypes")
                        final ExceptionEventImpl breadthFirstEvent = new ExceptionEventImpl(stack, true,
                                exceptionEvent.isHandled());

                        handler.notify(breadthFirstEvent);

                        LOG.fine(String.format("Handler %s returned status %s", handler,
                                breadthFirstEvent.getCurrentExceptionHandlingFlow().name()));

                        if (!breadthFirstEvent.isUnmute())
                        {
                            processedHandlers.add(handler);
                        }

                        switch (breadthFirstEvent.getCurrentExceptionHandlingFlow())
                        {
                            case HANDLED:
                                exceptionEvent.setHandled(true);
                                return;
                            case HANDLED_AND_CONTINUE:
                                exceptionEvent.setHandled(true);
                                break;
                            case ABORT:
                                return;
                            case SKIP_CAUSE:
                                exceptionEvent.setHandled(true);
                                stack.skipCause();
                                continue inbound_cause;
                            case THROW_ORIGINAL:
                                throwException = exceptionEvent.getException();
                                break;
                            case THROW:
                                throwException = breadthFirstEvent.getThrowNewException();
                                break;
                            default:
                                throw new IllegalStateException(
                                        "Unexpected enum type " + breadthFirstEvent.getCurrentExceptionHandlingFlow());
                        }
                    }
                }

                final Collection<HandlerMethod<? extends Throwable>> handlersForException =
                        handlerMethodStorage.getHandlersForException(stack.getCurrent().getClass(),
                                beanManager, exceptionEvent.getQualifiers(), false);

                final List<HandlerMethod<? extends Throwable>> depthFirstHandlerMethods =
                        new ArrayList<HandlerMethod<? extends Throwable>>(handlersForException);

                // Reverse these so category handlers are last
                Collections.reverse(depthFirstHandlerMethods);

                for (HandlerMethod<?> handler : depthFirstHandlerMethods)
                {
                    if (!processedHandlers.contains(handler))
                    {
                        LOG.fine(String.format("Notifying handler %s", handler));

                        @SuppressWarnings("rawtypes")
                        final ExceptionEventImpl depthFirstEvent = new ExceptionEventImpl(stack, false,
                                exceptionEvent.isHandled());
                        handler.notify(depthFirstEvent);

                        LOG.fine(String.format("Handler %s returned status %s", handler,
                                depthFirstEvent.getCurrentExceptionHandlingFlow().name()));

                        if (!depthFirstEvent.isUnmute())
                        {
                            processedHandlers.add(handler);
                        }

                        switch (depthFirstEvent.getCurrentExceptionHandlingFlow())
                        {
                            case HANDLED:
                                exceptionEvent.setHandled(true);
                                return;
                            case HANDLED_AND_CONTINUE:
                                exceptionEvent.setHandled(true);
                                break;
                            case ABORT:
                                return;
                            case SKIP_CAUSE:
                                exceptionEvent.setHandled(true);
                                stack.skipCause();
                                continue inbound_cause;
                            case THROW_ORIGINAL:
                                throwException = exceptionEvent.getException();
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

            if (!exceptionEvent.isHandled() && throwException == null)
            {
                LOG.warning(String.format("No handlers found for exception %s", exceptionEvent.getException()));
                throw exceptionEvent.getException();
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
            LOG.exiting(ExceptionHandlerDispatch.class.getName(), "executeHandlers", exceptionEvent.getException());
        }
    }
}
