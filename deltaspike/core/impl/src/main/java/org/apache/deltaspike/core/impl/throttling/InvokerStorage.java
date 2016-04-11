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
package org.apache.deltaspike.core.impl.throttling;


import org.apache.deltaspike.core.api.throttling.Throttled;
import org.apache.deltaspike.core.api.throttling.Throttling;
import org.apache.deltaspike.core.impl.util.AnnotatedMethods;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

@ApplicationScoped
@Typed(InvokerStorage.class)
public class InvokerStorage implements Throttling.SemaphoreFactory
{
    private final ConcurrentMap<String, Semaphore> semaphores = new ConcurrentHashMap<String, Semaphore>();
    private final ConcurrentMap<Method, Invoker> providers = new ConcurrentHashMap<Method, Invoker>();

    @Inject
    private BeanManager beanManager;

    Invoker getOrCreateInvoker(final InvocationContext ic)
    {
        final Method method = ic.getMethod();
        Invoker i = providers.get(method);
        if (i == null)
        {
            final Class declaringClass = method.getDeclaringClass();
            final AnnotatedType<Object> annotatedType = beanManager.createAnnotatedType(declaringClass);
            final AnnotatedMethod<?> annotatedMethod = AnnotatedMethods.findMethod(annotatedType, method);

            Throttled config = annotatedMethod.getAnnotation(Throttled.class);
            if (config == null)
            {
                config = annotatedType.getAnnotation(Throttled.class);
            }
            Throttling sharedConfig = annotatedMethod.getAnnotation(Throttling.class);
            if (sharedConfig == null)
            {
                sharedConfig = annotatedType.getAnnotation(Throttling.class);
            }

            final Throttling.SemaphoreFactory factory =
                    sharedConfig != null && sharedConfig.factory() != Throttling.SemaphoreFactory.class ?
                            Throttling.SemaphoreFactory.class.cast(
                                    beanManager.getReference(beanManager.resolve(
                                            beanManager.getBeans(
                                                    sharedConfig.factory())),
                                            Throttling.SemaphoreFactory.class, null)) : this;

            final Semaphore semaphore = factory.newSemaphore(
                    annotatedMethod,
                    sharedConfig != null && !sharedConfig.name().isEmpty() ?
                            sharedConfig.name() : declaringClass.getName(),
                    sharedConfig != null && sharedConfig.fair(),
                    sharedConfig != null ? sharedConfig.permits() : 1);
            final long timeout = config.timeoutUnit().toMillis(config.timeout());
            final int weigth = config.weight();
            i = new Invoker(semaphore, weigth, timeout);
            final Invoker existing = providers.putIfAbsent(ic.getMethod(), i);
            if (existing != null)
            {
                i = existing;
            }
        }
        return i;
    }

    @Override
    public Semaphore newSemaphore(final AnnotatedMethod<?> method, final String name,
                                  final boolean fair, final int permits)
    {
        Semaphore semaphore = semaphores.get(name);
        if (semaphore == null)
        {
            semaphore = new Semaphore(permits, fair);
            final Semaphore existing = semaphores.putIfAbsent(name, semaphore);
            if (existing != null)
            {
                semaphore = existing;
            }
        }
        return semaphore;
    }
}
