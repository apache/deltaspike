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

/**
 * Basic interface for all message-types
 */
public interface Message
{
    /**
     * @param messageBundleName a name of the message bundle which should be used
     * @return the instance of the current message context builder
     */
    Message bundle(String messageBundleName);

    /**
     * @param messageTemplate message key (or inline-text) for the current message
     * @return the current instance of the message builder to allow a fluent api
     */
    Message template(String messageTemplate);

    /**
     * @param arguments numbered and/or named argument(s) for the current message
     * @return the current instance of the message builder to allow a fluent api
     */
    Message argument(Object... arguments);

    /**
     * @return the configured message bundle name
     */
    String getBundle();

    /**
     * @return the message key (or inline-text) of the current message
     */
    String getTemplate();

    /**
     * @return all named and numbered arguments
     */
    Object[] getArguments();


}
