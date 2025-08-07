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
package org.apache.deltaspike.test.control;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.deltaspike.test.utils.Implementation;

/**
 * This annotation is used to define the {@link #implementations()} which the test is allowed to run.
 * 
 * If {@link #implementations()} is not defined, It will be used all available implementations
 * defined on {@link Implementation}.
 * 
 * A specific implementation can have {@link #versions()} range locked through the use of {@link LockedVersionRange}
 * @author rafaelbenevides
 * @author struberg
 *
 */
@Target(value = { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface LockedImplementation
{

    /**
     * If defined, the test will only run if one of those implementations get detected
     */
    Implementation[] implementations() default {};

    /**
     * Can be used to further restrict the implementation for {@link #implementations()}.
     */
    LockedVersionRange[] versions() default { };

    /**
     * If defined, the test will <b>NOT</b> run if one of those implementations get detected
     */
    Implementation[] excludedImplementations() default {};

    /**
     * Can be used to further restrict the implementation for {@link #excludedImplementations()} ()}.
     */
    LockedVersionRange[] excludedVersions() default {};
}
