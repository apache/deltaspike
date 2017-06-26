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
package org.apache.deltaspike.proxy.util;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import javax.enterprise.inject.spi.BeanManager;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.proxy.api.DeltaSpikeProxyFactory;
import org.apache.deltaspike.proxy.spi.DeltaSpikeProxy;
import org.apache.deltaspike.proxy.spi.invocation.DeltaSpikeProxyInvocationHandler;

public class EnableInterceptorsProxyFactory extends DeltaSpikeProxyFactory
{
    private static final EnableInterceptorsProxyFactory INSTANCE = new EnableInterceptorsProxyFactory();

    private EnableInterceptorsProxyFactory()
    {
        
    }

    public static <T> T wrap(T obj, BeanManager beanManager)
    {
        if (obj == null)
        {
            throw new IllegalArgumentException("obj must not be null!");
        }
        
        // generate proxy
        Class proxyClass = INSTANCE.getProxyClass(beanManager, obj.getClass());

        // delegate method calls to our original instance from the wrapped producer method
        EnableInterceptorsDelegate delegate = new EnableInterceptorsDelegate(obj);

        try
        {
            // instantiate proxy
            T proxy = (T) proxyClass.newInstance();
            
            DeltaSpikeProxy deltaSpikeProxy = (DeltaSpikeProxy) proxy;
            
            // TODO this can be optimized by caching this in a appscoped bean
            deltaSpikeProxy.setInvocationHandler(
                    BeanProvider.getContextualReference(DeltaSpikeProxyInvocationHandler.class));
            deltaSpikeProxy.setDelegateInvocationHandler(delegate);
            deltaSpikeProxy.setDelegateMethods(INSTANCE.getDelegateMethods(obj.getClass()));
            
            return proxy;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not create proxy instance by class " + obj.getClass(), e);
        }
    }
    
    @Override
    protected ArrayList<Method> getDelegateMethods(Class<?> targetClass, ArrayList<Method> allMethods)
    {
        // the default #filterInterceptMethods filters all non-public, final and abstract methods
        // which means actually every publich proxyable method
        // as we need to delegate method call to the original object instance -> proxy all public methods
        ArrayList<Method> delegateMethods = super.filterInterceptMethods(targetClass, allMethods);
        return delegateMethods;
    }

    @Override
    protected ArrayList<Method> filterInterceptMethods(Class<?> targetClass, ArrayList<Method> allMethods)
    {
        // we don't need to overwrite methods to just execute interceptors
        // all method call are delegated to our EnableInterceptorsDelegate, to delegate to the original object instance
        return null;
    }

    @Override
    protected String getProxyClassSuffix()
    {
        return "$$DSInterceptorProxy";
    }
    
    /**
     * {@link InvocationHandler} to delegate every method call to an provided object instance.
     */
    private static class EnableInterceptorsDelegate implements InvocationHandler, Serializable
    {
        private final Object instance;

        public EnableInterceptorsDelegate(Object instance)
        {
            this.instance = instance;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            return method.invoke(instance, args);
        }
    }
}
