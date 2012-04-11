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

package org.apache.deltaspike.core.impl.exception.control;

import org.apache.deltaspike.core.api.exception.control.HandlerMethod;

import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;

/**
 * Injectable storage to support programmatic registration and lookup of
 * {@link org.apache.deltaspike.core.api.exception.control.HandlerMethod} instances.
 */
//X TODO move it to the spi package - otherwise there is no need for an interface
public interface HandlerMethodStorage
{
    /**
     * Registers the given handlerMethod to the storage.
     *
     * @param handlerMethod HandlerMethod implementation to register with the storage
     */
    <T extends Throwable> void registerHandlerMethod(HandlerMethod<T> handlerMethod);

    /**
     * Obtains the applicable handlers for the given type or super type of the given type to order the handlers.
     *
     * @param exceptionClass    Type of exception to narrow handler list
     * @param bm                active BeanManager
     * @param handlerQualifiers additional handlerQualifiers to limit handlers
     * @param isBefore          traversal limiter
     * @return An order collection of handlers for the given type.
     */
    Collection<HandlerMethod<? extends Throwable>> getHandlersForException(Type exceptionClass, BeanManager bm,
                                                                           Set<Annotation> handlerQualifiers,
                                                                           boolean isBefore);
}
