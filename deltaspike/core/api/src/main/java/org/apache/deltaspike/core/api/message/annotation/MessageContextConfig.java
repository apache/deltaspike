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
package org.apache.deltaspike.core.api.message.annotation;

import org.apache.deltaspike.core.api.message.LocaleResolver;
import org.apache.deltaspike.core.api.message.MessageInterpolator;
import org.apache.deltaspike.core.api.message.MessageResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Allows to customize the message-resolution and processing
 * in combination with {@link MessageBundle}.
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface MessageContextConfig
{
    /**
     * Optional custom message-source.
     * @return classes of the message-sources
     */
    String[] messageSource() default { };

    /**
     * {@link MessageResolver} which should be used for resolving the message-template (= basic text)
     * @return class of the {@link MessageResolver}-bean or the default marker
     */
    Class<? extends MessageResolver> messageResolver() default MessageResolver.class;

    /**
     * {@link MessageInterpolator} which should be used for replacing the placeholders in the resolved text
     * @return class of the {@link MessageInterpolator}-bean or the default marker
     */
    Class<? extends MessageInterpolator> messageInterpolator() default MessageInterpolator.class;

    /**
     * {@link LocaleResolver} which should be used for providing the locale for resolving
     * the message-template (= basic text)
     * @return class of the {@link LocaleResolver}-bean or the default marker
     */
    Class<? extends LocaleResolver> localeResolver() default LocaleResolver.class;
}
