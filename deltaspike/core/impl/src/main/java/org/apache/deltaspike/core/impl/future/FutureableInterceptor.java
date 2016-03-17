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

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.future.Futureable;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Interceptor
@Futureable("")
public class FutureableInterceptor implements Serializable
{
    private static final Class<?> COMPLETION_STAGE;
    private static final Class<?> COMPLETABLE_FUTURE;
    private static final Method COMPLETABLE_STAGE_TO_FUTURE;
    private static final Method COMPLETABLE_FUTURE_COMPLETE;
    private static final Method COMPLETABLE_FUTURE_COMPLETE_ERROR;

    static
    {
        Class<?> completionStageClass = null;
        Class<?> completableFutureClass = null;
        Method completionStageClassToCompletableFuture = null;
        Method completableFutureComplete = null;
        Method completableFutureCompleteError = null;
        try
        {
            final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            completionStageClass = classLoader.loadClass("java.util.concurrent.CompletionStage");
            completionStageClassToCompletableFuture = completionStageClass.getMethod("toCompletableFuture");
            completableFutureClass = classLoader.loadClass("java.util.concurrent.CompletableFuture");
            completableFutureComplete = completableFutureClass.getMethod("complete", Object.class);
            completableFutureCompleteError = completableFutureClass.getMethod("completeExceptionally", Throwable.class);
        }
        catch (final Exception e)
        {
            // not on java 8
        }
        COMPLETION_STAGE = completionStageClass;
        COMPLETABLE_FUTURE = completableFutureClass;
        COMPLETABLE_STAGE_TO_FUTURE = completionStageClassToCompletableFuture;
        COMPLETABLE_FUTURE_COMPLETE = completableFutureComplete;
        COMPLETABLE_FUTURE_COMPLETE_ERROR = completableFutureCompleteError;
    }

    @Inject
    private ThreadPoolManager manager;

    @Inject
    private BeanManager beanManager;

    private transient ConcurrentMap<Method, ExecutorService> configByMethod =
            new ConcurrentHashMap<Method, ExecutorService>();

    @AroundInvoke
    public Object invoke(final InvocationContext ic) throws Exception
    {
        // validate usage
        final Class<?> returnType = ic.getMethod().getReturnType();
        if (!COMPLETION_STAGE.isAssignableFrom(returnType) && !Future.class.isAssignableFrom(returnType))
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
                    final Future<?> future = !COMPLETION_STAGE.isInstance(proceed) ?
                            Future.class.cast(proceed) :
                            Future.class.cast(COMPLETABLE_STAGE_TO_FUTURE.invoke(proceed));
                    /*
                    cancelHook.set(new Callable<Boolean>()
                    {
                        @Override
                        public Boolean call()
                        {
                            return future.cancel(true);
                        }
                    });
                    */
                    return future.get();
                }
                catch (final InvocationTargetException e)
                {
                    throw rethrow(e.getCause());
                }
                catch (final Exception e)
                {
                    throw rethrow(e);
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

    private RuntimeException rethrow(final Throwable cause)
    {
        if (RuntimeException.class.isInstance(cause))
        {
            return RuntimeException.class.cast(cause);
        }
        return new IllegalStateException(cause);
    }

    private ExecutorService getOrCreatePool(final InvocationContext ic)
    {
        final Method method = ic.getMethod();
        ExecutorService executorService = configByMethod.get(method);
        if (executorService == null)
        {
            final AnnotatedType<?> annotatedType = beanManager.createAnnotatedType(method.getDeclaringClass());
            AnnotatedMethod<?> annotatedMethod = null;
            for (final AnnotatedMethod<?> am : annotatedType.getMethods())
            {
                if (am.getJavaMember().equals(method))
                {
                    annotatedMethod = am;
                    break;
                }
            }
            if (annotatedMethod == null)
            {
                throw new IllegalStateException("No annotated method for " + method);
            }
            final Futureable methodConfig = annotatedMethod.getAnnotation(Futureable.class);
            final ExecutorService instance = manager.find(
                    (methodConfig == null ? annotatedType.getAnnotation(Futureable.class) : methodConfig).value());
            configByMethod.putIfAbsent(method, instance);
            executorService = instance;
        }
        return executorService;
    }

    @ApplicationScoped
    public static class ThreadPoolManager
    {
        private final ConcurrentMap<String, ExecutorService> pools = new ConcurrentHashMap<String, ExecutorService>();
        private volatile ExecutorService defaultPool;
        private volatile boolean closed = false;

        @PreDestroy
        private void shutdown()
        {
            closed = true;
            final String propertyValue = ConfigResolver.getPropertyValue("deltaspike.future.timeout");
            final long timeout = propertyValue == null ? TimeUnit.MINUTES.toMillis(1) : Integer.parseInt(propertyValue);
            for (final ExecutorService es : pools.values())
            {
                es.shutdown();
                try
                {
                    es.awaitTermination(timeout, TimeUnit.MILLISECONDS);
                }
                catch (final InterruptedException e)
                {
                    Thread.interrupted();
                }
            }
            if (defaultPool != null)
            {
                defaultPool.shutdown();
                try
                {
                    defaultPool.awaitTermination(timeout, TimeUnit.MILLISECONDS);
                }
                catch (final InterruptedException e)
                {
                    Thread.interrupted();
                }
            }
            pools.clear();
        }

        // open door for users until we have a config, should be part of API but since it can change keeping it there
        public void register(final String name, final ExecutorService es)
        {
            pools.putIfAbsent(name, es);
        }

        public ExecutorService find(final String name)
        {
            if (closed)
            {
                throw new IllegalStateException("Container is shutting down");
            }
            ExecutorService pool = pools.get(name);
            if (pool == null)
            {
                ensureDefaultPool();
                pool = defaultPool;
            }
            return pool;
        }

        private void ensureDefaultPool()
        {
            if (defaultPool == null)
            {
                synchronized (this)
                {
                    if (defaultPool == null)
                    {
                        defaultPool = Executors.newFixedThreadPool(
                                Math.max(2, Runtime.getRuntime().availableProcessors()));
                    }
                }
            }
        }
    }

    private static final class J8PromiseCompanionTask<T> implements Runnable
    {
        private Object dep;
        private Callable<T> fn;

        J8PromiseCompanionTask(final Object dep, Callable<T> fn)
        {
            this.dep = dep;
            this.fn = fn;
        }

        public void run()
        {
            try
            {
                COMPLETABLE_FUTURE_COMPLETE.invoke(dep, fn.call());
            }
            catch (final InvocationTargetException e)
            {
                try
                {
                    COMPLETABLE_FUTURE_COMPLETE_ERROR.invoke(dep, e.getCause());
                }
                catch (IllegalAccessException e1)
                {
                    throw new IllegalStateException(e1);
                }
                catch (final InvocationTargetException e1)
                {
                    throw new IllegalStateException(e1.getCause());
                }
            }
            catch (Exception e)
            {
                try
                {
                    COMPLETABLE_FUTURE_COMPLETE_ERROR.invoke(dep, e);
                }
                catch (IllegalAccessException e1)
                {
                    throw new IllegalStateException(e1);
                }
                catch (final InvocationTargetException e1)
                {
                    throw new IllegalStateException(e1.getCause());
                }
            }
        }
    }
}
