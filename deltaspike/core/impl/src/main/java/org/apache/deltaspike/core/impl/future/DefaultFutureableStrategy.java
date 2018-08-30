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
import java.util.Arrays;
import java.util.LinkedList;
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

    // only for weld1
    private static final boolean IS_WELD1;
    private static final ThreadLocal<LinkedList<CallKey>> STACK = new ThreadLocal<LinkedList<CallKey>>()
    {
        @Override
        protected LinkedList<CallKey> initialValue()
        {
            return new LinkedList<CallKey>();
        }
    };

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

        { // workaround for weld -> use a thread local to track the invocations
            boolean weld1 = false;
            try
            {
                final Class<?> impl = Thread.currentThread().getContextClassLoader()
                        .loadClass("org.jboss.weld.manager.BeanManagerImpl");
                final Package pck = impl.getPackage();
                weld1 = "Weld Implementation".equals(pck.getImplementationTitle())
                        && pck.getSpecificationVersion() != null && pck.getSpecificationVersion().startsWith("1.1.");
            }
            catch (final Throwable cnfe)
            {
                // no-op
            }
            IS_WELD1 = weld1;
        }
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
        final CallKey invocationKey;
        if (IS_WELD1)
        {
            invocationKey = new CallKey(ic);
            { // weld1 workaround
                final LinkedList<CallKey> stack = STACK.get();
                if (!stack.isEmpty() && stack.getLast().equals(invocationKey))
                {
                    try
                    {
                        return ic.proceed();
                    }
                    finally
                    {
                        if (stack.isEmpty())
                        {
                            STACK.remove();
                        }
                    }
                }
            }
        }
        else
        {
            invocationKey = null;
        }

        // validate usage
        final Class<?> returnType = ic.getMethod().getReturnType();
        if (!Future.class.isAssignableFrom(returnType) &&
                !void.class.isAssignableFrom(returnType) &&
                (COMPLETION_STAGE == null || !COMPLETION_STAGE.isAssignableFrom(returnType)))
        {
            throw new IllegalArgumentException("Return type should be a CompletableStage, Future or void");
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
                final LinkedList<CallKey> callStack;
                if (IS_WELD1)
                {
                    callStack = STACK.get();
                    callStack.add(invocationKey);
                }
                else
                {
                    callStack = null;
                }
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
                finally
                {
                    if (IS_WELD1)
                    {
                        callStack.removeLast();
                        if (callStack.isEmpty())
                        {
                            STACK.remove();
                        }
                    }
                }
            }
        };

        final ExecutorService pool = getOrCreatePool(ic);
        
        if (void.class.isAssignableFrom(returnType))
        {
            pool.submit(invocation);
            return null;
        }
        
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

    private static final class CallKey
    {
        private final InvocationContext ic;
        private final int hash;

        private CallKey(final InvocationContext ic)
        {
            this.ic = ic;

            final Object[] parameters = ic.getParameters();
            this.hash = ic.getMethod().hashCode() + (parameters == null ? 0 : Arrays.hashCode(parameters));
        }

        @Override
        public boolean equals(final Object o)
        {
            return this == o || !(o == null || getClass() != o.getClass()) && equals(ic, CallKey.class.cast(o).ic);
        }

        @Override
        public int hashCode()
        {
            return hash;
        }

        private boolean equals(final InvocationContext ic1, final InvocationContext ic2)
        {
            final Object[] parameters1 = ic1.getParameters();
            final Object[] parameters2 = ic2.getParameters();
            return ic2.getMethod().equals(ic1.getMethod()) &&
                    (parameters1 == parameters2 ||
                    (parameters1 != null && parameters2 != null && Arrays.equals(parameters1, ic2.getParameters())));
        }
    }
}
