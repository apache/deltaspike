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
package org.apache.deltaspike.core.api.message;

import java.io.Serializable;

/**
 * Implementations are responsible to replace placeholders in a message with the final value
 */
public interface MessageInterpolator extends Serializable
{
    /**
     * replaces the arguments of the given message with the given arguments
     *
     * instead of a MessageContextAware interface. we need it to avoid expensive operations like locking or deep cloning
     * @param messageText the message text which has to be interpolated
     * @param arguments a list of numbered and/or named arguments for the current message
     * @return the final (interpolated) message text
     *         if it was possible to replace the parameters with the given arguments
     *         the unmodified messageText otherwise
     */
    String interpolate(String messageText, Object[] arguments);
}
