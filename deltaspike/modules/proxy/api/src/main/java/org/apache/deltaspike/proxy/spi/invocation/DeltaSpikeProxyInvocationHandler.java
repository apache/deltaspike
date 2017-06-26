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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Interceptor;
import javax.inject.Inject;
import org.apache.deltaspike.core.util.ReflectionUtils;
import org.apache.deltaspike.proxy.api.DeltaSpikeProxyFactory;
import org.apache.deltaspike.proxy.spi.DeltaSpikeProxy;

/**
 * The {@link InvocationHandler} which will be called directly by the proxy methods.
 * For both <code>delegateMethods</code> and <code>interceptMethods</code>
 * (See: {@link org.apache.deltaspike.proxy.spi.DeltaSpikeProxyClassGenerator}).
 * 
 * This {@link InvocationHandler} first executes CDI interceptors (if defined on method or class level) and
 * after that the original method or the {@link DeltaSpikeProxy#getDelegateInvocationHandler()} will be executed,
 * depending if the invoked method is a <code>intercept</code> or <code>delegate</code> method.
 */
@ApplicationScoped
public class DeltaSpikeProxyInvocationHandler implements InvocationHandler
{
    @Inject
    private BeanManager beanManager;
    
    @Inject
    private DeltaSpikeProxyInterceptorLookup interceptorLookup;

    @Override
    public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable
    {
        // check if interceptors are defined, otherwise just call the original logik
        List<Interceptor<?>> interceptors = interceptorLookup.lookup(proxy, method);
        if (interceptors != null && !interceptors.isEmpty())
        {
            try
            {
                DeltaSpikeProxyInvocationContext invocationContext = new DeltaSpikeProxyInvocationContext(
                        this, beanManager, interceptors, proxy, method, parameters, null);

                Object returnValue = invocationContext.proceed();

                if (invocationContext.isProceedOriginal())
                {
                    return invocationContext.getProceedOriginalReturnValue();
                }

                return returnValue;
            }
            catch (DeltaSpikeProxyInvocationWrapperException e)
            {
                throw e.getCause();
            }
        }

        return proceed(proxy, method, parameters);
    }

    /**
     * Calls the original method or delegates to {@link DeltaSpikeProxy#getDelegateInvocationHandler()}
     * after invoking the interceptor chain.
     *
     * @param proxy The current proxy instance.
     * @param method The current invoked method.
     * @param parameters The method parameter.
     * @return The original value from the original method.
     * @throws Throwable
     */
    protected Object proceed(Object proxy, Method method, Object[] parameters) throws Throwable
    {
        DeltaSpikeProxy deltaSpikeProxy = (DeltaSpikeProxy) proxy;

        if (contains(deltaSpikeProxy.getDelegateMethods(), method))
        {
            return deltaSpikeProxy.getDelegateInvocationHandler().invoke(proxy, method, parameters);
        }
        else
        {
            try
            {
                Method superAccessorMethod = DeltaSpikeProxyFactory.getSuperAccessorMethod(proxy, method);
                return superAccessorMethod.invoke(proxy, parameters);
            }
            catch (InvocationTargetException e)
            {
                // rethrow original exception
                throw e.getCause();
            }
        }
    }
    
    protected boolean contains(Method[] methods, Method method)
    {
        if (methods == null || methods.length == 0)
        {
            return false;
        }
        
        for (Method current : methods)
        {
            if (ReflectionUtils.hasSameSignature(method, current))
            {
                return true;
            }
        }
        
        return false;
    }
}
