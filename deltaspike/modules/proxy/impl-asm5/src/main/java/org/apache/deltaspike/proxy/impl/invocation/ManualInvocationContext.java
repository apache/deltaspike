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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.InvocationContext;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;

/**
 * {@link InvocationContext} implementation to support manual interceptor invocation before invoking the
 * original logic via the given {@link AbstractManualInvocationHandler}.
 */
@Typed
public class ManualInvocationContext<T, H> implements InvocationContext
{
    protected List<Interceptor<H>> interceptors;
    protected int interceptorIndex;
    protected T target;
    protected Method method;
    protected Object[] parameters;
    protected Map<String, Object> contextData;
    protected Object timer;
    protected AbstractManualInvocationHandler manualInvocationHandler;

    protected BeanManager beanManager;

    protected boolean proceedOriginal;
    protected Object proceedOriginalReturnValue;

    public ManualInvocationContext(AbstractManualInvocationHandler manualInvocationHandler,
            List<Interceptor<H>> interceptors, T target, Method method, Object[] parameters, Object timer)
    {
        this.manualInvocationHandler = manualInvocationHandler;
        this.interceptors = interceptors;
        this.target = target;
        this.method = method;
        this.parameters = parameters;
        this.timer = timer;

        this.interceptorIndex = 0;
    }

    @Override
    public Object getTarget()
    {
        return target;
    }

    @Override
    public Method getMethod()
    {
        return method;
    }

    @Override
    public Object[] getParameters()
    {
        return parameters;
    }

    @Override
    public void setParameters(Object[] os)
    {
        parameters = os;
    }

    @Override
    public Map<String, Object> getContextData()
    {
        if (contextData == null)
        {
            contextData = new HashMap<String, Object>();
        }
        return contextData;
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
                // lazy init beanManager
                if (beanManager == null)
                {
                    beanManager = BeanManagerProvider.getInstance().getBeanManager();
                }

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
            proceedOriginalReturnValue = manualInvocationHandler.proceedOriginal(target, method, parameters);
        }
        catch (Exception e)
        {
            throw e;
        }
        catch (Throwable e)
        {
            // wrap the Throwable here as interceptors declared only "throws Exception"
            throw new ManualInvocationThrowableWrapperException(e);
        }

        return null;
    }

    @Override
    public Object getTimer()
    {
        return timer;
    }

    // @Override
    // CDI 1.1 compatibility
    public Constructor getConstructor()
    {
        return null;
    }

    public boolean isProceedOriginal()
    {
        return proceedOriginal;
    }

    public Object getProceedOriginalReturnValue()
    {
        return proceedOriginalReturnValue;
    }

    public void setProceedOriginalReturnValue(Object proceedOriginalReturnValue)
    {
        this.proceedOriginalReturnValue = proceedOriginalReturnValue;
    }
}
