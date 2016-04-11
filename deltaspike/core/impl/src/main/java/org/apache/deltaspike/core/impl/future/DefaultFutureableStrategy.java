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
package org.apache.deltaspike.core.impl.future;

import org.apache.deltaspike.core.api.future.Futureable;
import org.apache.deltaspike.core.impl.util.AnnotatedMethods;
import org.apache.deltaspike.core.spi.future.FutureableStrategy;
import org.apache.deltaspike.core.util.ExceptionUtils;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Dependent
public class DefaultFutureableStrategy implements FutureableStrategy
{
    private static final Class<?> COMPLETION_STAGE;
    private static final Class<?> COMPLETABLE_FUTURE;
    private static final Method COMPLETABLE_STAGE_TO_FUTURE;

    static
    {
        Class<?> completionStageClass = null;
        Class<?> completableFutureClass = null;
        Method completionStageClassToCompletableFuture = null;
        try
        {
            final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            completionStageClass = classLoader.loadClass("java.util.concurrent.CompletionStage");
            completionStageClassToCompletableFuture = completionStageClass.getMethod("toCompletableFuture");
            completableFutureClass = classLoader.loadClass("java.util.concurrent.CompletableFuture");
        }
        catch (final Exception e)
        {
            // not on java 8
        }
        COMPLETION_STAGE = completionStageClass;
        COMPLETABLE_FUTURE = completableFutureClass;
        COMPLETABLE_STAGE_TO_FUTURE = completionStageClassToCompletableFuture;
    }

    @Inject
    private ThreadPoolManager manager;

    @Inject
    private BeanManager beanManager;

    private transient ConcurrentMap<Method, ExecutorService> configByMethod =
        new ConcurrentHashMap<Method, ExecutorService>();


    @Override
    public Object execute(final InvocationContext ic) throws Exception
    {
        // validate usage
        final Class<?> returnType = ic.getMethod().getReturnType();
        if (!Future.class.isAssignableFrom(returnType) &&
                (COMPLETION_STAGE == null || !COMPLETION_STAGE.isAssignableFrom(returnType)))
        {
            throw new IllegalArgumentException("Return type should be a CompletableStage or Future");
        }

        if (configByMethod == null)
        {
            synchronized (this)
            {
                if (configByMethod == null)
                {
                    configByMethod = new ConcurrentHashMap<Method, ExecutorService>();
                }
            }
        }

        // running < j8 we cant have cancellation
        //final AtomicReference<Callable<?>> cancelHook = new AtomicReference<Callable<?>>();
        final Callable<Object> invocation = new Callable<Object>()
        {
            @Override
            public Object call() throws Exception
            {
                try
                {
                    final Object proceed = ic.proceed();
                    final Future<?> future = COMPLETION_STAGE == null || !COMPLETION_STAGE.isInstance(proceed) ?
                            Future.class.cast(proceed) :
                            Future.class.cast(COMPLETABLE_STAGE_TO_FUTURE.invoke(proceed));
                    return future.get();
                }
                catch (final InvocationTargetException e)
                {
                    throw ExceptionUtils.throwAsRuntimeException(e.getCause());
                }
                catch (final Exception e)
                {
                    throw ExceptionUtils.throwAsRuntimeException(e);
                }
            }
        };

        final ExecutorService pool = getOrCreatePool(ic);
        if (COMPLETABLE_FUTURE == null)  // not on java 8 can only be a future
        {
            return pool.submit(invocation);
        }

        // java 8, use CompletableFuture, it impl CompletionStage and Future so everyone is happy
        final Object completableFuture = COMPLETABLE_FUTURE.newInstance();
        pool.submit(new J8PromiseCompanionTask(completableFuture, invocation));
        // TODO: handle cancel
        return completableFuture;
    }

    protected ExecutorService getOrCreatePool(final InvocationContext ic)
    {
        final Method method = ic.getMethod();
        ExecutorService executorService = configByMethod.get(method);
        if (executorService == null)
        {
            final AnnotatedType<?> annotatedType = beanManager.createAnnotatedType(method.getDeclaringClass());
            final AnnotatedMethod<?> annotatedMethod = AnnotatedMethods.findMethod(annotatedType, method);
            final Futureable methodConfig = annotatedMethod.getAnnotation(Futureable.class);
            final ExecutorService instance = manager.find(
                    (methodConfig == null ? annotatedType.getAnnotation(Futureable.class) : methodConfig).value());
            configByMethod.putIfAbsent(method, instance);
            executorService = instance;
        }
        return executorService;
    }
}
