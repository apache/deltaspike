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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Configures message resolution and processing of a {@link MessageBundle}.
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface MessageContextConfig
{
    /**
     * <p>Additional message source.</p>
     *
     * <p>A message source is a lookup hint for the {@link MessageResolver}. For the default MessageResolver this is the
     * name of the {@link java.util.ResourceBundle}.</p>
     *
     * <p>Example: To use 2 additional ResourceBundles for the lookup, you can configure the MessageContextConfig like
     * this:
     * <pre>
     *  &#064;MessageBundle
     *  &#064;MessageContextConfig(messageSource = {"mycomp.ErrorMessages","mycomp.BusinessMessages"})
     *  public interface MyCompanyMessages {...
     * </pre>.
     * </p>
     *
     * @return classes of the message-sources
     */
    String[] messageSource() default { };

    /**
     * {@link MessageResolver} to use for resolution of message templates to message text.
     *
     * @return class of the {@link MessageResolver} bean or the default marker
     */
    Class<? extends MessageResolver> messageResolver() default MessageResolver.class;

    /**
     * {@link MessageInterpolator} to use for interpolation of placeholders in the resolved text.
     *
     * @return class of the {@link MessageInterpolator} bean or the default marker
     */
    Class<? extends MessageInterpolator> messageInterpolator() default MessageInterpolator.class;

    /**
     * {@link LocaleResolver} providing the locale for message template resolution.
     *
     * @return class of the {@link LocaleResolver} bean or the default marker
     */
    Class<? extends LocaleResolver> localeResolver() default LocaleResolver.class;
}
