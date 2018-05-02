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
package org.apache.deltaspike.proxy.spi.invocation;

import java.lang.reflect.Method;
import java.util.List;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;

import org.apache.deltaspike.core.util.interceptor.AbstractInvocationContext;

/**
 * {@link javax.interceptor.InvocationContext} implementation to support interceptor invocation
 * before proceed with the original method or delegation.
 */
@Typed
public class DeltaSpikeProxyInvocationContext<T, H> extends AbstractInvocationContext<T>
{
    protected List<Interceptor<H>> interceptors;
    protected int interceptorIndex;
    protected DeltaSpikeProxyInvocationHandler invocationHandler;

    protected BeanManager beanManager;

    protected boolean proceedOriginal;
    protected Object proceedOriginalReturnValue;

    public DeltaSpikeProxyInvocationContext(DeltaSpikeProxyInvocationHandler invocationHandler,
            BeanManager beanManager, List<Interceptor<H>> interceptors, 
            T target, Method method, Object[] parameters, Object timer)
    {
        super(target, method, parameters, timer);

        this.invocationHandler = invocationHandler;
        this.interceptors = interceptors;
        this.beanManager = beanManager;

        this.interceptorIndex = 0;
    }

    @Override
    public Object proceed() throws Exception
    {
        if (proceedOriginal)
        {
            return null;
        }

        if (interceptors.size() > interceptorIndex)
        {
            Interceptor<H> interceptor = null;
            CreationalContext<H> creationalContext = null;
            H interceptorInstance = null;

            try
            {
                interceptor = interceptors.get(interceptorIndex++);
                creationalContext = beanManager.createCreationalContext(interceptor);
                interceptorInstance = interceptor.create(creationalContext);

                return interceptor.intercept(InterceptionType.AROUND_INVOKE, interceptorInstance, this);
            }
            finally
            {
                if (creationalContext != null)
                {
                    if (interceptorInstance != null && interceptor != null)
                    {
                        interceptor.destroy(interceptorInstance, creationalContext);
                    }

                    creationalContext.release();
                }
            }
        }


        // workaround for OWB 1.1, otherwise we could just return the proceedOriginalReturnValue here
        try
        {
            proceedOriginal = true;
            proceedOriginalReturnValue = invocationHandler.proceed(target, method, parameters);
        }
        catch (Exception e)
        {
            throw e;
        }
        catch (Throwable e)
        {
            // wrap the Throwable here as interceptors declared only "throws Exception"
            throw new DeltaSpikeProxyInvocationWrapperException(e);
        }

        return proceedOriginalReturnValue;
    }

    public boolean isProceedOriginal()
    {
        return proceedOriginal;
    }

    public Object getProceedOriginalReturnValue()
    {
        return proceedOriginalReturnValue;
    }
}
