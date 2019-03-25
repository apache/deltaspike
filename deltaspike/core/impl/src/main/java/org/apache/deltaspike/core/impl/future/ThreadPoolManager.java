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
import org.apache.deltaspike.core.api.config.base.CoreBaseConfig;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

@ApplicationScoped
public class ThreadPoolManager
{
    private final ConcurrentMap<String, ExecutorService> pools = new ConcurrentHashMap<String, ExecutorService>();
    private final Collection<CreationalContext<?>> contexts = new ArrayList<CreationalContext<?>>(8);
    private volatile boolean closed = false;

    @Inject
    private BeanManager beanManager;

    @PreDestroy
    private void shutdown()
    {
        closed = true;
        final long timeout = CoreBaseConfig.TimeoutCustomization.FUTUREABLE_TERMINATION_TIMEOUT_IN_MILLISECONDS;
        for (final ExecutorService es : pools.values())
        {
            es.shutdown();
        }
        for (final ExecutorService es : pools.values())
        {
            try
            {
                es.awaitTermination(timeout, TimeUnit.MILLISECONDS);
            }
            catch (final InterruptedException e)
            {
                Thread.interrupted();
            }
        }
        pools.clear();

        for (final CreationalContext<?> ctx : contexts)
        {
            ctx.release();
        }
        contexts.clear();
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
            synchronized (this)
            {
                pool = pools.get(name);
                if (pool == null)
                {
                    // the instantiation does the following:
                    // 1. check if there is a named bean matching this name using @Default qualifier
                    // 2. check if there is a JNDI entry (ManagedExecutorService case) matching this name
                    // 3. create a new executor service based on the DS-config

                    // 1.
                    final Set<Bean<?>> beans = beanManager.getBeans(name);
                    if (beans != null && !beans.isEmpty())
                    {
                        final Bean<?> bean = beanManager.resolve(beans);
                        if (bean.getTypes().contains(ExecutorService.class))
                        {
                            final CreationalContext<Object> creationalContext =
                                    beanManager.createCreationalContext(null);
                            if (!beanManager.isNormalScope(bean.getScope()))
                            {
                                contexts.add(creationalContext);
                            }
                            pool = ExecutorService.class.cast(beanManager.getReference(
                                    bean, ExecutorService.class, creationalContext));
                        }
                    }

                    if (pool == null) // 2.
                    {
                        for (final String prefix : asList(
                                "", "java:app/", "java:global/", "java:global/threads/",
                                "java:global/deltaspike/", "java:"))
                        {
                            try
                            {
                                final Object instance = new InitialContext().lookup(prefix + name);
                                if (ExecutorService.class.isInstance(instance))
                                {
                                    pool = ExecutorService.class.cast(instance);
                                    break;
                                }
                            }
                            catch (final NamingException e)
                            {
                                // no-op
                            }
                        }
                    }

                    if (pool == null) // 3.
                    {
                        final String configPrefix = "futureable.pool." + name + ".";
                        final int coreSize = ConfigResolver.resolve(configPrefix + "coreSize")
                                .as(Integer.class)
                                .withDefault(Math.max(2, Runtime.getRuntime().availableProcessors()))
                                .getValue();
                        final int maxSize = ConfigResolver.resolve(configPrefix + "maxSize")
                                .as(Integer.class)
                                .withDefault(coreSize)
                                .getValue();
                        final long keepAlive = ConfigResolver.resolve(configPrefix + "keepAlive.value")
                                .as(Long.class)
                                .withDefault(0L)
                                .getValue();
                        final String keepAliveUnit = ConfigResolver.resolve(configPrefix + "keepAlive.unit")
                                .as(String.class)
                                .withDefault("MILLISECONDS")
                                .getValue();

                        final String queueType = ConfigResolver.resolve(configPrefix + "queue.type")
                                .as(String.class)
                                .withDefault("LINKED")
                                .getValue();
                        final BlockingQueue<Runnable> queue;
                        if ("ARRAY".equalsIgnoreCase(queueType))
                        {
                            final int size = ConfigResolver.resolve(configPrefix + "queue.size")
                                    .as(Integer.class)
                                    .withDefault(1024)
                                    .getValue();
                            final boolean fair = ConfigResolver.resolve(configPrefix + "queue.fair")
                                    .as(Boolean.class)
                                    .withDefault(false)
                                    .getValue();
                            queue = new ArrayBlockingQueue<Runnable>(size, fair);
                        }
                        else if ("SYNCHRONOUS".equalsIgnoreCase(queueType))
                        {
                            final boolean fair = ConfigResolver.resolve(configPrefix + "queue.fair")
                                    .as(Boolean.class)
                                    .withDefault(false)
                                    .getValue();
                            queue = new SynchronousQueue<Runnable>(fair);
                        }
                        else
                        {
                            final int capacity = ConfigResolver.resolve(configPrefix + "queue.capacity")
                                    .as(Integer.class)
                                    .withDefault(Integer.MAX_VALUE)
                                    .getValue();
                            queue = new LinkedBlockingQueue<Runnable>(capacity);
                        }

                        final String threadFactoryName = ConfigResolver.getPropertyValue(
                                configPrefix + "threadFactory.name");
                        final ThreadFactory threadFactory;
                        if (threadFactoryName != null)
                        {
                            threadFactory = lookupByName(threadFactoryName, ThreadFactory.class);
                        }
                        else
                        {
                            threadFactory = Executors.defaultThreadFactory();
                        }

                        final String rejectedHandlerName = ConfigResolver.getPropertyValue(
                                configPrefix + "rejectedExecutionHandler.name");
                        final RejectedExecutionHandler rejectedHandler;
                        if (rejectedHandlerName != null)
                        {
                            rejectedHandler = lookupByName(rejectedHandlerName, RejectedExecutionHandler.class);
                        }
                        else
                        {
                            rejectedHandler = new ThreadPoolExecutor.AbortPolicy();
                        }

                        pool = new ThreadPoolExecutor(
                                coreSize, maxSize,
                                keepAlive, TimeUnit.valueOf(keepAliveUnit),
                                queue, threadFactory, rejectedHandler);
                    }

                    pools.put(name, pool);
                }
            }
        }
        return pool;
    }

    private <T> T lookupByName(final String name, final Class<T> type)
    {
        final Set<Bean<?>> tfb = beanManager.getBeans(name);
        final Bean<?> bean = beanManager.resolve(tfb);
        final CreationalContext<?> ctx = beanManager.createCreationalContext(null);
        if (!beanManager.isNormalScope(bean.getScope()))
        {
            contexts.add(ctx);
        }
        return type.cast(beanManager.getReference(bean, type, ctx));
    }
}

