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

import org.apache.deltaspike.core.api.exception.control.ExceptionHandlingFlow;
import org.apache.deltaspike.core.api.exception.control.event.ExceptionStackEvent;
import org.apache.deltaspike.core.spi.exception.control.event.IntrospectiveExceptionEvent;

import javax.enterprise.inject.Typed;

/**
 * Payload for an exception to be handled.  This object is not immutable as small pieces of the state may be set by the
 * handler.
 *
 * @param <T> Exception type this event represents
 */
@Typed()
public class DefaultExceptionEvent<T extends Throwable> implements IntrospectiveExceptionEvent<T>
{
    private final T exception;
    private boolean unmute;
    private ExceptionHandlingFlow flow;
    private Throwable throwNewException;
    private final boolean beforeTraversal;
    private final boolean markedHandled;


    /**
     * Initial state constructor.
     *
     * @param stackEvent           Information about the current exception and cause chain.
     * @param beforeTraversal flag indicating the direction of the cause chain traversal
     * @param handled         flag indicating the exception has already been handled by a previous handler
     * @throws IllegalArgumentException if stackEvent is null
     */
    public DefaultExceptionEvent(final ExceptionStackEvent stackEvent, final boolean beforeTraversal,
                                 final boolean handled)
    {
        if (stackEvent == null)
        {
            throw new IllegalArgumentException("null is not valid for stackEvent");
        }

        exception = (T) stackEvent.getCurrent();
        this.beforeTraversal = beforeTraversal;
        markedHandled = handled;
        flow = ExceptionHandlingFlow.HANDLED_AND_CONTINUE;
    }

    @Override
    public T getException()
    {
        return exception;
    }

    @Override
    public void abort()
    {
        flow = ExceptionHandlingFlow.ABORT;
    }

    @Override
    public void throwOriginal()
    {
        flow = ExceptionHandlingFlow.THROW_ORIGINAL;
    }

    @Override
    public void handled()
    {
        flow = ExceptionHandlingFlow.HANDLED;
    }

    @Override
    public void handledAndContinue()
    {
        flow = ExceptionHandlingFlow.HANDLED_AND_CONTINUE;
    }

    @Override
    public void skipCause()
    {
        flow = ExceptionHandlingFlow.SKIP_CAUSE;
    }

    @Override
    public void unmute()
    {
        unmute = true;
    }

    @Override
    public boolean isUnmute()
    {
        return unmute;
    }

    /* Later
    public ExceptionStackEvent getExceptionStack() {
    }
    */

    @Override
    public ExceptionHandlingFlow getCurrentExceptionHandlingFlow()
    {
        return flow;
    }

    @Override
    public boolean isMarkedHandled()
    {
        return markedHandled;
    }

    @Override
    public boolean isBeforeTraversal()
    {
        return beforeTraversal;
    }

    @Override
    public void rethrow(Throwable t)
    {
        throwNewException = t;
        flow = ExceptionHandlingFlow.THROW;
    }

    @Override
    public Throwable getThrowNewException()
    {
        return throwNewException;
    }
}
