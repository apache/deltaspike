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
 * Central context for handling dynamic messages
 */
public interface MessageContext extends LocaleResolver, Serializable
{
    /**
     * @return message builder to add and/or create a new message based on the current context via a fluent api
     */
    MessageBuilder message();

    /**
     * @return the current config to change it or create a new one base on the current config
     */
    Config config();

    /*
     * @param messageTemplate a message which should be added to the current context (message handlers)
     */
    //void addMessage(Message messageTemplate);

    /**
     * Helper for building instances of {@link Message}
     */
    interface MessageBuilder
    {
        /**
         * @param messageTemplate message key (or inline-text) for the current message
         * @return the current instance of the message builder to allow a fluent api
         */
        MessageBuilder text(String messageTemplate);

        /**
         * @param arguments numbered and/or named argument(s) for the current message
         * @return the current instance of the message builder to allow a fluent api
         */
        MessageBuilder argument(Object... arguments);

        /**
         * @return the message which was built via the fluent api
         */
        Message create();

        /**
         * @return the text of the message which was built via the fluent api
         */
        String toText();
    }

    /**
     * Config for customizing a {@link MessageContext}
     */
    interface Config extends Serializable
    {
        /**
         * create a new context based on the default context - the default context won't get modified
         *
         * @return a message context builder based on the current config
         */
        MessageContextBuilder use();

        /**
         * change the default context
         *
         * @return a message context builder to change the current config
         */
        MessageContextBuilder change();

        /**
         * @return the current message interpolator
         */
        MessageInterpolator getMessageInterpolator();

        /**
         * @return the current message resolver
         */
        MessageResolver getMessageResolver();

        /**
         * @return the current locale resolver
         */
        LocaleResolver getLocaleResolver();

        interface MessageContextBuilder
        {
            /**
             * @param messageInterpolator a new message interpolator
             * @return the instance of the current message context builder
             */
            MessageContextBuilder messageInterpolator(MessageInterpolator messageInterpolator);

            /**
             * @param messageResolver a new message resolver
             * @return the instance of the current message context builder
             */
            MessageContextBuilder messageResolver(MessageResolver messageResolver);

            /**
             * @param localeResolver a new locale resolver
             * @return the instance of the current message context builder
             */
            MessageContextBuilder localeResolver(LocaleResolver localeResolver);

            /**
             * @return a new message context based on the current config
             */
            MessageContext create();
        }
    }
}
