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
import java.util.List;

/**
 * Central context for handling dynamic messages.
 * <br/>
 * Instances of this type are mutable but also {@link Cloneable}. If you need a new instance, then use
 * {@link Object#clone()}.
 */
public interface MessageContext extends LocaleResolver, Serializable, Cloneable
{
    /**
     * Clones the current MessageContext.
     */
    MessageContext clone();

    /**
     * @return a message based on the current context modifiable via a fluent API
     */
    Message message();

    /**
     * Configures a message source instance for use by a {@link MessageResolver}.
     *
     * @param messageSource message source to add
     *
     * @return the instance of the current message context builder
     */
    MessageContext messageSource(String... messageSource);

    /**
     * @param messageInterpolator a new message interpolator to be set
     * @return the instance of the current message context builder
     */
    MessageContext messageInterpolator(MessageInterpolator messageInterpolator);

    /**
     * @param messageResolver a new message resolver to be set
     * @return the instance of the current message context builder
     */
    MessageContext messageResolver(MessageResolver messageResolver);

    /**
     * @param localeResolver a new locale resolver to be set
     * @return the instance of the current message context builder
     */
    MessageContext localeResolver(LocaleResolver localeResolver);

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

    /**
     * @return list of registered message sources
     */
    List<String> getMessageSources();
}
