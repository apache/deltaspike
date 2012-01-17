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

package org.apache.deltaspike.core.api.metadata;

import org.apache.deltaspike.core.api.metadata.builder.AnnotatedTypeBuilder;

import java.lang.annotation.Annotation;

/**
 * An implementation {@link AnnotationRedefiner} can be applied to an
 * {@link AnnotatedTypeBuilder}, and receives callbacks for each annotation of
 * the type is it applied for.
 *
 * @see AnnotatedTypeBuilder
 */
public interface AnnotationRedefiner<A extends Annotation>
{
    /**
     * Callback invoked for each annotation of the type the
     * {@link AnnotationRedefiner} is applied for.
     *
     * @param ctx
     */
    public void redefine(RedefinitionContext<A> ctx);

}
