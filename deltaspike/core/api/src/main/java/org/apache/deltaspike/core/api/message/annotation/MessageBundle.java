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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Marker annotation for a message-bundle interface which provides type-safe messages.
 * Each method on such an interface will form a type-safe message. The message lookup key
 * (resource bundle key) can either be defined by annotating those methods
 * with &#064;{@link MessageTemplate}) or by convention. if no &#064;{@link MessageTemplate}
 * annotation is used on a method, it's name will be used as resource key.</p>
 *
 * <p>This annotation must only be used on interfaces.
 * If this annotation gets used on a concrete class, a deployment error results!</p>
 *
 * <p>The {@link java.util.ResourceBundle} or other resource lookup source is
 * determined by the {@link org.apache.deltaspike.core.api.message.MessageResolver}
 * in conjunction with
 * {@link org.apache.deltaspike.core.api.message.MessageContext#messageSource(String...)}.
 * The fully qualified class name of the interface annotated with
 * &#064;MessageBundle will automatically be registered as additional <code>messageSource</code>!</p>
 *
 * <p>Can be combined with {@link MessageContextConfig} to customize the
 * message-resolution and processing.</p>
 */
@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface MessageBundle
{
}
