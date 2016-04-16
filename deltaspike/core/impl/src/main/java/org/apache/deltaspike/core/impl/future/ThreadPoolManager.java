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

import org.apache.deltaspike.core.api.config.base.CoreBaseConfig;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ThreadPoolManager
{
    private final ConcurrentMap<String, ExecutorService> pools = new ConcurrentHashMap<String, ExecutorService>();
    private volatile ExecutorService defaultPool;
    private volatile boolean closed = false;

    @PreDestroy
    private void shutdown()
    {
        closed = true;
        final long timeout = CoreBaseConfig.TimeoutCustomization.FUTUREABLE_TERMINATION_TIMEOUT_IN_MILLISECONDS;
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

