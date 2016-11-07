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
package org.apache.deltaspike.core.api.config;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Marker to let DeltaSpike pick this interface and create a proxy getting values
 * from the configuration.
 *
 * The underlying Bean should be normal-scoped.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Configuration
{
    /**
     * @return the duration while the value is not reloaded.
     */
    long cacheFor() default -1;

    /**
     * @return the duration unit for {@see cacheFor()}.
     */
    TimeUnit cacheUnit() default SECONDS;

    /**
     * @return the key prefix to apply to all methods.
     */
    String prefix() default "";
}
