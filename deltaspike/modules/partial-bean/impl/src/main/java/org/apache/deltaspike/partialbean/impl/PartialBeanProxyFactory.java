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
package org.apache.deltaspike.partialbean.impl;

import org.apache.deltaspike.proxy.api.DeltaSpikeProxyFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;

import javax.enterprise.inject.Typed;

/**
 * {@link DeltaSpikeProxyFactory} which delegates all abstract methods to the 
 * partial bean binding {@link java.lang.reflect.InvocationHandler}.
 */
@Typed
public class PartialBeanProxyFactory extends DeltaSpikeProxyFactory
{
    private static final PartialBeanProxyFactory INSTANCE = new PartialBeanProxyFactory();
    
    public static PartialBeanProxyFactory getInstance()
    {
        return INSTANCE;
    }
    
    @Override
    protected String getProxyClassSuffix()
    {
        return "$$DSPartialBeanProxy";
    }

    @Override
    protected ArrayList<Method> getDelegateMethods(Class<?> targetClass, ArrayList<Method> allMethods)
    {
        ArrayList<Method> methods = new ArrayList<>();
        
        Iterator<Method> it = allMethods.iterator();
        while (it.hasNext())
        {
            Method method = it.next();

            if (Modifier.isAbstract(method.getModifiers()))
            {
                methods.add(method);
            }
        }
        
        return methods;
    }
}
