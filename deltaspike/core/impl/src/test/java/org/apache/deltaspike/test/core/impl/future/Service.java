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
package org.apache.deltaspike.test.core.impl.future;

import org.apache.deltaspike.core.api.future.Futureable;

import javax.enterprise.context.ApplicationScoped;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class Service
{

    @Futureable
    public void thatSLong(final long sleep, CountDownLatch latch) {
    	try
        {
            Thread.sleep(sleep);
            latch.countDown();
        }
        catch (final InterruptedException e)
        {
            Thread.interrupted();
            throw new IllegalStateException(e);
        }
    }

    @Futureable // or CompletableFuture<String>
    public Future<String> thatSLong(final long sleep)
    {
        try
        {
            Thread.sleep(sleep);
            // return CompletableFuture.completedFuture("done");
            return new Future<String>()  // EE will have AsyncFuture but more designed for j8 ^^
            {
                @Override
                public boolean cancel(final boolean mayInterruptIfRunning)
                {
                    return false;
                }

                @Override
                public boolean isCancelled()
                {
                    return false;
                }

                @Override
                public boolean isDone()
                {
                    return true;
                }

                @Override
                public String get() throws InterruptedException, ExecutionException
                {
                    return "done";
                }

                @Override
                public String get(final long timeout, final TimeUnit unit)
                        throws InterruptedException, ExecutionException, TimeoutException
                {
                    return "done";
                }
            };
        }
        catch (final InterruptedException e)
        {
            Thread.interrupted();
            throw new IllegalStateException(e);
        }
    }
}
