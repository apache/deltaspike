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
package org.apache.deltaspike.core.api.config.view.metadata;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A ConfigDescriptor can contain CallbackDescriptors or ExecutableCallbackDescriptors. An ExecutableCallbackDescriptor
 * can reference one or more callback method(s). If there is only one callback type, it's possible to annotate it with
 * {@code @DefaultCallback}. That eliminates the need for a special marker annotation for the target method.
 *
 * If there are multiple callback types, it's necessary to use custom annotations as marker for the target method (e.g.
 * see {@code @Secured} vs. {@code @ViewControllerRef}).
 *
 * <pre>
 * {@code
 * ViewConfigDescriptor viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(SomePage.class);
 *
 * viewConfigDescriptor.getExecutableCallbackDescriptor(
 *   Secured.class, Secured.Descriptor.class).execute(accessDecisionVoterContext);
 * }</pre> is short for
 * <pre>
 * {@code
 * viewConfigDescriptor.getExecutableCallbackDescriptor(
 *   Secured.class, DefaultCallback.class, Secured.Descriptor.class).execute(accessDecisionVoterContext);
 * }</pre>
 *
 * whereas e.g.
 * <pre>
 * {@code
 * viewConfigDescriptor.getExecutableCallbackDescriptor(
 *   ViewControllerRef.class, PreRenderView.class, ViewControllerRef.Descriptor.class).execute();
 * }</pre> or just
 * <pre>
 * {@code
 * viewConfigDescriptor.getExecutableCallbackDescriptor(
 *   ViewControllerRef.class, PreRenderView.class, SimpleCallbackDescriptor.class).execute();
 * }</pre> are needed to call @PreRenderView callbacks specifically (instead of the others like @InitView which are also
 * bound to @ViewControllerRef).
 */
//TODO find a better name
@Target( METHOD )
@Retention(RUNTIME)
@Documented
public @interface DefaultCallback 
{
}
