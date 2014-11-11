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
 * Implementations have to resolve the text stored for a given key in the message source they are aware of.
 * Implementations should always be &#064;Dependent scoped!
 */
public interface MessageResolver extends Serializable
{
    String MISSING_RESOURCE_MARKER = "???";

    /**
     *
     * @param messageContext  messageContext which should be used
     * @param messageTemplate the message key (or inline text) of the current message
     * @param category        the category of the message, e.g. 'longText'. Can be <code>null</code>
     *
     * @return the final but not interpolated message text or <code>null</code> if an error happened or the resource
     *         could not be resolved.
     */
    String getMessage(MessageContext messageContext, String messageTemplate, String category);
}
