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

import org.apache.deltaspike.core.api.exception.control.event.ExceptionEvent;

import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Metadata interface for an exception handler method. It is the responsibility of the
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
    boolean isBeforeHandler();

    /**
     * Calls the handler method, passing the given event object.
     *
     * @param event event to pass to the handler.
     * @param beanManager The BeanManager to use
     */
    void notify(ExceptionEvent<T> event, BeanManager beanManager) throws Exception;

    /**
     * Obtains the precedence of the handler, relative to other handlers for the same type. Handler with a higher
     * ordinal is invoked before a handler with a lower ordinal.
     */
    int getOrdinal();

    /**
     * Basic {@link Object#equals(Object)} but must use all of the get methods from this interface to
     * maintain compatibility.
     *
     * @param o Object being compared to this.
     * @return true or false based on standard equality.
     */
    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}
