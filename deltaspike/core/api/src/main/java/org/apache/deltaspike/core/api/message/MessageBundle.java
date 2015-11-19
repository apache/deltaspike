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

import javax.enterprise.inject.Stereotype;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marker annotation for a message-bundle interface which provides type-safe messages.
 *
 * <p>
 * This annotation must only be used on interfaces. If this annotation gets used on a concrete class, a deployment error
 * results!</p>
 *
 * <h3>Type-safe Messages</h3>
 * <p>
 * Each method on an interface annotated with <code>&#064;MessageBundle</code> will form a type-safe message. The
 * message lookup key (resource bundle key) can either be defined by annotating those methods with
 * &#064;{@link MessageTemplate}) or by convention. if no &#064;{@link MessageTemplate} annotation is used on a method,
 * the case sensitive method name name will be used as resource key.</p>
 *
 * <h3>Message Parameters</h3>
 * <p>
 * The parameters of the declared methods will be automatically passed as message parameters to the
 * {@link org.apache.deltaspike.core.api.message.MessageResolver}. Please note that all passed parameters should be
 * {@link java.io.Serializable}. If a parameter is not Serializable, we will instead store the <code>toString()</code>
 * of the passed parameter.</p>
 *
 *
 * <h3>Message Sources</h3>
 * <p>
 * The {@link java.util.ResourceBundle} or other resource lookup source which might be used is determined by the
 * {@link org.apache.deltaspike.core.api.message.MessageResolver} in conjunction with
 * {@link org.apache.deltaspike.core.api.message.MessageContext#messageSource(String...)}. The fully qualified class
 * name of the interface annotated with &#064;MessageBundle will automatically be registered as additional
 * <code>messageSource</code> for those messages.</p>
 * <p>
 * By default the Message Source is a resource bundle with the same name as the {@code &#064;MessageBundle}
 * annotated interface, e.g. {@code com.acme.MyMessages_en.properties}.
 * </p>
 *
 * <p>
 * <code>&#064;MessageBundle</code> can be combined with {@link MessageContextConfig} to further customize the
 * message resolution and processing. To use a different resourcebundle, e.g. 
 * {@code somepath/myownmessages_en.properties} you might write:
 * <pre>
 * &#064;MessageBundle
 * &#064;MessageContextConfig(messageSource = "somepath/myownmessages")
 * </pre>
 *
 * </p>
 *
 * <p>
 * Debug hint: Set a breakpoint in <code>MessageBundleInvocationHandler#invoke</code>. This will get called for every
 * message bundle invocation.</p>
 */
@Stereotype

@Target({ TYPE })
@Retention(RUNTIME)
@Documented
public @interface MessageBundle
{
}
