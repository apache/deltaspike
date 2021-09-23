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

package org.apache.deltaspike.core.spi.exception.control.event;

import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;
import org.apache.deltaspike.core.api.exception.control.ExceptionHandlingFlow;

/**
 * Internal view into the ExceptionEvent. Methods on this interface are used by the ExceptionHandlerBroadcaster.
 */
public interface IntrospectiveExceptionEvent<T extends Throwable> extends ExceptionEvent<T>
{
    /**
     * Check to see if this event has been unmuted and therefore called again.
     */
    boolean isUnmute();

    /**
     * The next expected step in the exception handling flow (i.e. abort, rethrow, etc)
     */
    ExceptionHandlingFlow getCurrentExceptionHandlingFlow();

    boolean isBeforeTraversal();

    /**
     * Returns the exception that should be thrown if the next step in the flow is THROW.
     */
    Throwable getThrowNewException();
}
