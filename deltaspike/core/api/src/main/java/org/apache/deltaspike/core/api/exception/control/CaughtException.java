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

package org.apache.deltaspike.core.api.exception.control;

import javax.enterprise.inject.Typed;

/**
 * Payload for an exception to be handled.  This object is not immutable as small pieces of the state may be set by the
 * handler.
 *
 * @param <T> Exception type this event represents
 */
@SuppressWarnings({"unchecked", "CdiManagedBeanInconsistencyInspection"})
@Typed()
public class CaughtException<T extends Throwable>
{
    /**
     * Flow control enum.  Used in the dispatcher to determine how to markHandled.
     */
    protected enum ExceptionHandlingFlow
    {
        HANDLED,
        HANDLED_AND_CONTINUE,
        SKIP_CAUSE,
        ABORT,
        THROW_ORIGINAL,
        THROW
    }

    private final ExceptionStack exceptionStack;
    private final T exception;
    private boolean unmute;
    private ExceptionHandlingFlow flow;
    private Throwable throwNewException;
    private final boolean beforeTraversal;
    private final boolean markedHandled;


    /**
     * Initial state constructor.
     *
     * @param exceptionStack  Information about the current exception and cause chain.
     * @param beforeTraversal flag indicating the direction of the cause chain traversal
     * @param handled         flag indicating the exception has already been handled by a previous handler
     * @throws IllegalArgumentException if exceptionStack is null
     */
    public CaughtException(final ExceptionStack exceptionStack, final boolean beforeTraversal, final boolean handled)
    {
        if (exceptionStack == null)
        {
            throw new IllegalArgumentException("null is not valid for exceptionStack");
        }

        this.exception = (T) exceptionStack.getCurrent();
        this.exceptionStack = exceptionStack;
        this.beforeTraversal = beforeTraversal;
        this.markedHandled = handled;
        this.flow = ExceptionHandlingFlow.HANDLED_AND_CONTINUE;
    }

    public T getException()
    {
        return this.exception;
    }

    /**
     * Instructs the dispatcher to abort further processing of handlers.
     */
    public void abort()
    {
        this.flow = ExceptionHandlingFlow.ABORT;
    }

    /**
     * Instructs the dispatcher to throw the original exception after handler processing.
     */
    public void throwOriginal()
    {
        this.flow = ExceptionHandlingFlow.THROW_ORIGINAL;
    }

    /**
     * Instructs the dispatcher to terminate additional handler processing and mark the event as handled.
     */
    public void handled()
    {
        this.flow = ExceptionHandlingFlow.HANDLED;
    }

    /**
     * Default instruction to dispatcher, continues handler processing.
     */
    public void handledAndContinue()
    {
        this.flow = ExceptionHandlingFlow.HANDLED_AND_CONTINUE;
    }

    /**
     * Similar to {@link org.apache.deltaspike.core.api.exception.control.CaughtException#handledAndContinue()},
     * but instructs the dispatcher to markHandled to the next element
     * in the cause chain without processing additional handlers for this cause chain element.
     */
    public void skipCause()
    {
        this.flow = ExceptionHandlingFlow.SKIP_CAUSE;
    }

    /**
     * Instructs the dispatcher to allow this handler to be invoked again.
     */
    public void unmute()
    {
        this.unmute = true;
    }

    protected boolean isUnmute()
    {
        return this.unmute;
    }

    /* Later
    public ExceptionStack getExceptionStack() {
    }
    */

    protected ExceptionHandlingFlow getFlow()
    {
        return this.flow;
    }

    public boolean isMarkedHandled()
    {
        return this.isMarkedHandled();
    }

    /**
     * Rethrow the exception, but use the given exception instead of the original.
     *
     * @param t Exception to be thrown in place of the original.
     */
    public void rethrow(Throwable t)
    {
        this.throwNewException = t;
        this.flow = ExceptionHandlingFlow.THROW;
    }

    protected Throwable getThrowNewException()
    {
        return this.throwNewException;
    }
}
