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

package org.apache.deltaspike.core.api.exception.control.event;

/**
 * Payload for an exception to be handled. Implementations of this interface should not expose internals and should
 * remain immutable.
 *
 * @param <T> Exception type this event represents
 */
public interface ExceptionEvent<T extends Throwable>
{
    /**
     * The exception causing this event.
     */
    T getException();

    /**
     * Instructs the dispatcher to abort further processing of handlers.
     */
    void abort();

    /**
     * Instructs the dispatcher to throw the original exception after handler processing.
     */
    void throwOriginal();

    /**
     * Instructs the dispatcher to terminate additional handler processing and mark the event as handled.
     */
    void handled();

    /**
     * Default instruction to dispatcher, continues handler processing.
     */
    void handledAndContinue();

    /**
     * Similar to {@link ExceptionEvent#handledAndContinue()},
     * but instructs the dispatcher to markHandled to the next element
     * in the cause chain without processing additional handlers for this cause chain element.
     */
    void skipCause();

    /**
     * Instructs the dispatcher to allow this handler to be invoked again.
     */
    void unmute();

    /**
     * Rethrow the exception, but use the given exception instead of the original.
     *
     * @param t Exception to be thrown in place of the original.
     */
    void rethrow(Throwable t);

    /**
     * Check to see if this exception has been handled.
     */
    boolean isMarkedHandled();
}
