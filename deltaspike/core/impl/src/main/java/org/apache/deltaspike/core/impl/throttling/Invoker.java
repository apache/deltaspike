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

import org.apache.deltaspike.core.util.ExceptionUtils;

import javax.interceptor.InvocationContext;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

class Invoker
{
    private final int weight;
    private final Semaphore semaphore;
    private final long timeout;

    Invoker(final Semaphore semaphore, final int weight, final long timeout)
    {
        this.semaphore = semaphore;
        this.weight = weight;
        this.timeout = timeout;
    }

    public Object invoke(final InvocationContext context) throws Exception
    {
        if (timeout > 0)
        {
            try
            {
                if (!semaphore.tryAcquire(weight, timeout, TimeUnit.MILLISECONDS))
                {
                    throw new IllegalStateException(
                        "Can't acquire " + weight + " permits for " + context.getMethod() + " in " + timeout + "ms");
                }
            }
            catch (final InterruptedException e)
            {
                return onInterruption(e);
            }
        }
        else
        {
            try
            {
                semaphore.acquire(weight);
            }
            catch (final InterruptedException e)
            {
                return onInterruption(e);
            }
        }
        try
        {
            return context.proceed();
        }
        finally
        {
            semaphore.release(weight);
        }
    }

    private static Semaphore onInterruption(final InterruptedException e)
    {
        Thread.interrupted();
        throw ExceptionUtils.throwAsRuntimeException(e);
    }
}
