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

import org.apache.deltaspike.core.api.exception.control.CaughtException;
import org.apache.deltaspike.core.api.exception.control.ExceptionStack;
import org.apache.deltaspike.core.api.exception.control.ExceptionToCatch;
import org.apache.deltaspike.core.api.exception.control.HandlerMethod;
import org.apache.deltaspike.core.impl.exception.control.extension.CatchExtension;

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
public class ExceptionHandlerDispatch implements java.io.Serializable
{
    private static final long serialVersionUID = -144788905363939591L;

    private ExceptionToCatch exceptionToCatch;
    private ExceptionStack exceptionStack;

    private final Logger log = Logger.getLogger(this.getClass().toString());

    /**
     * Observes the event, finds the correct exception handler(s) and invokes them.
     *
     * @param eventException exception to be invoked
     * @param bm             active bean manager
     * @throws Throwable If a handler requests the exception to be re-thrown.
     */
    @SuppressWarnings({"unchecked", "MethodWithMultipleLoops", "ThrowableResultOfMethodCallIgnored"})
    public void executeHandlers(@Observes @Any ExceptionToCatch eventException, final BeanManager bm) throws Throwable
    {
        log.entering(ExceptionHandlerDispatch.class.getName(), "executeHandlers", eventException.getException());

        CreationalContext<Object> ctx = null;
        this.exceptionToCatch = eventException;

        Throwable throwException = null;

        final HandlerMethodStorage handlerMethodStorage = CatchExtension.createStorage();

        try
        {
            ctx = bm.createCreationalContext(null);

            final Set<HandlerMethod<?>> processedHandlers = new HashSet<HandlerMethod<?>>();

            final ExceptionStack stack = new ExceptionStack(eventException.getException());

            bm.fireEvent(stack); // Allow for modifying the exception stack

            inbound_cause:
            while (stack.getCurrent() != null)
            {
                this.exceptionStack = stack;

                final List<HandlerMethod<?>> breadthFirstHandlerMethods = new ArrayList<HandlerMethod<?>>(
                        handlerMethodStorage.getHandlersForException(stack.getCurrent().getClass(),
                                bm, eventException.getQualifiers(), true));

                for (HandlerMethod<?> handler : breadthFirstHandlerMethods)
                {
                    if (!processedHandlers.contains(handler))
                    {
                        log.fine(String.format("Notifying handler %s", handler));

                        @SuppressWarnings("rawtypes")
                        final CaughtException breadthFirstEvent = new CaughtException(stack, true,
                                eventException.isHandled());
                        handler.notify(breadthFirstEvent);

                        log.fine(String.format("Handler %s returned status %s", handler,
                                breadthFirstEvent.getFlow().name()));

                        if (!breadthFirstEvent.isUnmute())
                        {
                            processedHandlers.add(handler);
                        }

                        switch (breadthFirstEvent.getFlow())
                        {
                            case HANDLED:
                                eventException.setHandled(true);
                                return;
                            case HANDLED_AND_CONTINUE:
                                eventException.setHandled(true);
                                break;
                            case ABORT:
                                return;
                            case SKIP_CAUSE:
                                eventException.setHandled(true);
                                stack.skipCause();
                                continue inbound_cause;
                            case THROW_ORIGINAL:
                                throwException = eventException.getException();
                                break;
                            case THROW:
                                throwException = breadthFirstEvent.getThrowNewException();
                                break;
                            default:
                                throw new IllegalStateException("Unexpected enum type " + breadthFirstEvent.getFlow());
                        }
                    }
                }

                final Collection<HandlerMethod<? extends Throwable>> handlersForException =
                        handlerMethodStorage.getHandlersForException(stack.getCurrent().getClass(),
                                bm, eventException.getQualifiers(), false);

                final List<HandlerMethod<? extends Throwable>> depthFirstHandlerMethods =
                        new ArrayList<HandlerMethod<? extends Throwable>>(handlersForException);

                // Reverse these so category handlers are last
                Collections.reverse(depthFirstHandlerMethods);

                for (HandlerMethod<?> handler : depthFirstHandlerMethods)
                {
                    if (!processedHandlers.contains(handler))
                    {
                        log.fine(String.format("Notifying handler %s", handler));

                        @SuppressWarnings("rawtypes")
                        final CaughtException depthFirstEvent = new CaughtException(stack, false,
                                eventException.isHandled());
                        handler.notify(depthFirstEvent);

                        log.fine(String.format("Handler %s returned status %s", handler,
                                depthFirstEvent.getFlow().name()));

                        if (!depthFirstEvent.isUnmute())
                        {
                            processedHandlers.add(handler);
                        }

                        switch (depthFirstEvent.getFlow())
                        {
                            case HANDLED:
                                eventException.setHandled(true);
                                return;
                            case HANDLED_AND_CONTINUE:
                                eventException.setHandled(true);
                                break;
                            case ABORT:
                                return;
                            case SKIP_CAUSE:
                                eventException.setHandled(true);
                                stack.skipCause();
                                continue inbound_cause;
                            case THROW_ORIGINAL:
                                throwException = eventException.getException();
                                break;
                            case THROW:
                                throwException = depthFirstEvent.getThrowNewException();
                                break;
                            default:
                                throw new IllegalStateException("Unexpected enum type " + depthFirstEvent.getFlow());
                        }
                    }
                }
                this.exceptionStack.skipCause();
            }

            if (!eventException.isHandled() && throwException == null)
            {
                log.warning(String.format("No handlers found for exception %s", eventException.getException()));
                throw eventException.getException();
            }

            if (throwException != null)
            {
                throw throwException;
            }
        }
        finally
        {
            if (ctx != null)
            {
                ctx.release();
            }
        }

        log.exiting(ExceptionHandlerDispatch.class.getName(), "executeHandlers", exceptionToCatch.getException());
    }

    /* TODO: Later
    @Produces
    @ConversationScoped
    @Named("handledException")
    public ExceptionStack getExceptionStack() {
        return this.exceptionStack == null ? new ExceptionStack() : this.exceptionStack;
    }

    @Produces
    @ConversationScoped
    @Named("caughtException")
    public ExceptionToCatch getExceptionToCatch() {
        return this.exceptionToCatch == null ? new ExceptionToCatch() : this.exceptionToCatch;
    }
    */
}
