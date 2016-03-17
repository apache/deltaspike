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
package org.apache.deltaspike.core.api.throttling;

import javax.enterprise.util.Nonbinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.enterprise.inject.spi.AnnotatedMethod;
import java.util.concurrent.Semaphore;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Configure the throttler associated to the class/method.
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface Throttling
{
    /**
     * @return how to get the semaphore. Default to a plain Semaphore of the JVM.
     */
    @Nonbinding
    Class<? extends SemaphoreFactory> factory() default SemaphoreFactory.class;

    /**
     * @return true if the semaphore is fair false otherwise.
     */
    @Nonbinding
    boolean fair() default false;

    /**
     * @return how many permits has the semaphore.
     */
    @Nonbinding
    int permits() default 1;

    /**
     * @return name/bucket of this configuration (allow to have multiple buckets per class but default is 1 per class).
     */
    @Nonbinding
    String name() default "";

    interface SemaphoreFactory
    {
        /**
         * @param method the intercepted method.
         * @param name bucket name.
         * @param fair should the semaphore be fair.
         * @param permits maximum permits the semaphore shoulg get.
         * @return the semaphore build accordingly the parameters.
         */
        Semaphore newSemaphore(AnnotatedMethod<?> method, String name, boolean fair, int permits);
    }
}
