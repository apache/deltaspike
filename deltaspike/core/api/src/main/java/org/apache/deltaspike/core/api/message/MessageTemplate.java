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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Provides the message (template) for type-safe messages.
 *
 * <p>
 * This only works on interfaces which are annotated with {@link MessageBundle}.</p>
 *
 * <p>
 * Depending on the {@link org.apache.deltaspike.core.api.message.MessageResolver} this message template value might be
 * used as key to lookup internationalized values from a {@link java.util.ResourceBundle}.</p>
 *
 * <p>
 * A MessageTemplate value which starts and ends with brackets '{', '}' will be interpreted as key for resolving from a
 * ResourceBundle or any other lookup mechanism determined by the
 * {@link org.apache.deltaspike.core.api.message.MessageResolver}. A small example:
 * <pre>
 * &#064;MessageTemplate("{welcome_to}")
 * </pre> This will lookup a <code>welcome_to = Hello to Aruba</code> from the configured resource bundle.
 * </p>
 *
 * <p>
 * MessageTemplate values without '{', '}' bracelets will be directly used without resource lookup.</p>
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface MessageTemplate
{
    /**
     * The default format string of this message.
     *
     * @return the format string
     */
    String value();
}
