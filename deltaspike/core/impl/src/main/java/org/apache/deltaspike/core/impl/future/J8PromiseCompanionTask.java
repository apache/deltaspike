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

import org.apache.deltaspike.core.util.ExceptionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

class J8PromiseCompanionTask<T> implements Runnable
{
    private static final Method COMPLETABLE_FUTURE_COMPLETE;
    private static final Method COMPLETABLE_FUTURE_COMPLETE_ERROR;

    static
    {
        Class<?> completableFutureClass = null;
        Method completableFutureComplete = null;
        Method completableFutureCompleteError = null;
        try
        {
            final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            completableFutureClass = classLoader.loadClass("java.util.concurrent.CompletableFuture");
            completableFutureComplete = completableFutureClass.getMethod("complete", Object.class);
            completableFutureCompleteError = completableFutureClass.getMethod("completeExceptionally", Throwable.class);
        }
        catch (final Exception e)
        {
            // not on java 8
        }
        COMPLETABLE_FUTURE_COMPLETE = completableFutureComplete;
        COMPLETABLE_FUTURE_COMPLETE_ERROR = completableFutureCompleteError;
    }

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
                throw ExceptionUtils.throwAsRuntimeException(e1);
            }
            catch (final InvocationTargetException e1)
            {
                throw ExceptionUtils.throwAsRuntimeException(e1.getCause());
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
                throw ExceptionUtils.throwAsRuntimeException(e1);
            }
            catch (final InvocationTargetException e1)
            {
                throw ExceptionUtils.throwAsRuntimeException(e1.getCause());
            }
        }
    }
}
