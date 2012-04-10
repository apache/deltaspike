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

package org.apache.deltaspike.core.api.exception.control;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Meta data interface about an exception handler. It is the responsibility of the
 * implementation to support {@link javax.enterprise.inject.spi.InjectionPoint}s and to
 * validate those {@link javax.enterprise.inject.spi.InjectionPoint}s.
 *
 * @param <T> Exception for which this handler is responsible
 */
public interface HandlerMethod<T extends Throwable>
{
    /**
     * Obtains the set of handled event qualifiers.
     */
    Set<Annotation> getQualifiers();

    /**
     * Obtains the handled event type.
     */
    Type getExceptionType();

    /**
     * Flag indicating this handler should be invoked during the before traversal.
     */
    boolean isBefore();

    /**
     * Calls the handler method, passing the given event object.
     *
     * @param event event to pass to the handler.
     */
    //fields will be injected via BeanProvider#injectFields
    void notify(CaughtException<T> event);

    /**
     * Obtains the precedence of the handler.
     */
    int getOrdinal();

    /**
     * Basic {@link Object#equals(Object)} but must use all of the get methods from this interface to
     * maintain compatibility.
     *
     * @param o Object being compared to this.
     * @return true or false based on standard equality.
     */
    boolean equals(Object o);

    @Override
    int hashCode();
}
