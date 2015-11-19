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
import java.util.Collection;

/**
 * Basic interface for all messages.
 *
 * <p>
 * A <code>Message</code> is not a simple String but all the information needed to create those Strings for multiple
 * situations. The situation is determined by the used {@link MessageContext}.</p>
 */
public interface Message extends Serializable
{
    /**
     * @param messageTemplate message key (or plain text) for the current message
     *
     * @return the current instance of the message builder to allow a fluent API
     */
    Message template(String messageTemplate);

    /**
     * @param arguments numbered and/or named argument(s) for the current message
     *
     * @return the current instance of the message builder to allow a fluent API
     */
    Message argument(Serializable... arguments);
    
    /**
     * Argument array. Similar to argument except it is meant to handle an array being passed in via a chain.
     *
     * @param arguments the arguments
     *
     * @return the message
     */
    Message argumentArray(Serializable[] arguments);
    
    /**
     * Argument. Similar to the other argument methods, this one handles collections.
     *
     * @param arguments the arguments
     *
     * @return the message
     */
    Message argument(Collection<Serializable> arguments);

    /**
     * @return the message key (or plain text) of the current message
     */
    String getTemplate();

    /**
     * @return all named and numbered arguments
     */
    Object[] getArguments();

    /**
     * Renders the Message to a String, using the {@link MessageContext} which created the Message.
     */
    String toString();

    /**
     * Renders the Message to a String, using an arbitrary {@link MessageContext}.
     */
    String toString(MessageContext messageContext);

    /**
     * Renders the Message to a String, using the {@link MessageContext} which created the Message. While resolving the
     * message we will first search for a messageTemplate with the given category by just adding an underscore '_' and
     * the category String to the {@link #getTemplate()}. If no such template exists we will fall back to the version
     * without the category String.
     * <p>
     * DeltaSpike JSF messages e.g. distinguish between categories
     * {@code &quot;summary&quot;} and {@code &quot;detail&quot;}
     * to allow a short and a more detailed explanation in Error, Warn and Info popups at the same time.
     * </p>
     */
    String toString(String category);

    /**
     * Renders the Message to a String, using an arbitrary {@link MessageContext}. While resolving the message we will
     * first search for a messageTemplate with the given category by just adding an underscore '_' and the category
     * String to the {@link #getTemplate()}. If no such template exists we will fall back to the version without the
     * category String.
     */
    String toString(MessageContext messageContext, String category);



}
