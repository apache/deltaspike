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
package org.apache.deltaspike.proxy.spi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import javax.enterprise.inject.spi.BeanManager;
import org.apache.deltaspike.proxy.api.DeltaSpikeProxyFactory;

public class EnableInterceptorsProxyFactory extends DeltaSpikeProxyFactory
{
    private static final EnableInterceptorsProxyFactory INSTANCE = new EnableInterceptorsProxyFactory();
    
    public static EnableInterceptorsProxyFactory getInstance()
    {
        return INSTANCE;
    }
   
    public static <T> T wrap(T obj, BeanManager beanManager)
    {
        if (obj == null)
        {
            throw new IllegalArgumentException("obj must not be null!");
        }
        
        // generate proxy
        Class proxyClass = EnableInterceptorsProxyFactory.getInstance().getProxyClass(beanManager,
                obj.getClass(), EnableInterceptorsDelegate.class);

        // delegate method calls to our original instance from the wrapped producer method
        EnableInterceptorsDelegate delegate = new EnableInterceptorsDelegate(obj);

        try
        {
            // instantiate proxy
            Constructor constructor = proxyClass.getConstructor(EnableInterceptorsDelegate.class);
            return (T) constructor.newInstance(delegate);
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
}
