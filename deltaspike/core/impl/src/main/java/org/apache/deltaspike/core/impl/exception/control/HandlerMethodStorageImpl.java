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
import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.util.HierarchyDiscovery;

import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Basic implementation for {@link HandlerMethodStorage}.
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Typed()
class HandlerMethodStorageImpl implements HandlerMethodStorage
{
    private final Map<Type, Collection<HandlerMethod<? extends Throwable>>> allHandlers;

    private Logger log = Logger.getLogger(HandlerMethodStorageImpl.class.toString());

    HandlerMethodStorageImpl(Map<Type, Collection<HandlerMethod<? extends Throwable>>> allHandlers)
    {
        this.allHandlers = allHandlers;
    }

    @Override
    public <T extends Throwable> void registerHandlerMethod(HandlerMethod<T> handlerMethod)
    {
        log.fine(String.format("Adding handler %s to known handlers", handlerMethod));
        if (allHandlers.containsKey(handlerMethod.getExceptionType()))
        {
            allHandlers.get(handlerMethod.getExceptionType()).add(handlerMethod);
        }
        else
        {
            allHandlers.put(handlerMethod.getExceptionType(),
                    new HashSet<HandlerMethod<? extends Throwable>>(Collections.singleton(handlerMethod)));
        }
    }

    @Override
    public Collection<HandlerMethod<? extends Throwable>> getHandlersForException(Type exceptionClass,
                                                                                  BeanManager bm,
                                                                                  Set<Annotation> handlerQualifiers,
                                                                                  boolean isBefore)
    {
        final Collection<HandlerMethod<? extends Throwable>> returningHandlers =
                new TreeSet<HandlerMethod<? extends Throwable>>(new ExceptionHandlerComparator());
        final HierarchyDiscovery h = new HierarchyDiscovery(exceptionClass);
        final Set<Type> closure = h.getTypeClosure();

        for (Type hierarchyType : closure)
        {
            if (allHandlers.get(hierarchyType) != null)
            {
                for (HandlerMethod<?> handler : allHandlers.get(hierarchyType))
                {
                    if (handler.isBeforeHandler() && isBefore)
                    {
                        if (handler.getQualifiers().contains(new AnyLiteral()))
                        {
                            returningHandlers.add(handler);
                        }
                        else
                        {
                            if (!handlerQualifiers.isEmpty() && handlerQualifiers.equals(handler.getQualifiers()))
                            {
                                returningHandlers.add(handler);
                            }
                        }
                    }
                    else if (!handler.isBeforeHandler() && !isBefore)
                    {
                        if (handler.getQualifiers().contains(new AnyLiteral()))
                        {
                            returningHandlers.add(handler);
                        }
                        else
                        {
                            if (!handlerQualifiers.isEmpty() && handlerQualifiers.equals(handler.getQualifiers()))
                            {
                                returningHandlers.add(handler);
                            }
                        }
                    }
                }
            }
        }

        log.fine(String.format("Found handlers %s for exception type %s, qualifiers %s", returningHandlers,
                exceptionClass, handlerQualifiers));
        return Collections.unmodifiableCollection(returningHandlers);
    }
}
