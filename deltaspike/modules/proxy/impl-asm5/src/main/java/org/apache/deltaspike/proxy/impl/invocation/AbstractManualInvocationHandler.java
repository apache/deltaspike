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
package org.apache.deltaspike.proxy.impl.invocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import javax.enterprise.inject.spi.Interceptor;
import org.apache.deltaspike.core.api.provider.BeanProvider;

public abstract class AbstractManualInvocationHandler implements InvocationHandler
{
    private volatile Boolean initialized;
    private InterceptorLookup interceptorLookup;
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable
    {
        lazyInit();

        // check if interceptors are defined, otherwise just call the original logik
        List<Interceptor<?>> interceptors = interceptorLookup.lookup(proxy, method);
        if (interceptors != null && !interceptors.isEmpty())
        {
            try
            {
                ManualInvocationContext invocationContext =
                        new ManualInvocationContext(this, interceptors, proxy, method, parameters, null);

                Object returnValue = invocationContext.proceed();

                if (invocationContext.isProceedOriginal())
                {
                    return invocationContext.getProceedOriginalReturnValue();
                }

                return returnValue;
            }
            catch (ManualInvocationThrowableWrapperException e)
            {
                throw e.getCause();
            }
        }

        return proceedOriginal(proxy, method, parameters);
    }

    /**
     * Calls the original logic after invoking the interceptor chain.
     * 
     * @param proxy The current proxy instance.
     * @param method The current invoked method.
     * @param parameters The method parameter.
     * @return The original value from the original method.
     * @throws Throwable 
     */
    protected abstract Object proceedOriginal(Object proxy, Method method, Object[] parameters) throws Throwable;

    
    
    private void lazyInit()
    {
        if (this.initialized == null)
        {
            init();
        }
    }

    private synchronized void init()
    {
        // switch into paranoia mode
        if (this.initialized == null)
        {
            this.initialized = true;
            
            this.interceptorLookup = BeanProvider.getContextualReference(InterceptorLookup.class);
        }
    }
}
