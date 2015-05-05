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
package org.apache.deltaspike.jsf.impl.injection.proxy;

import org.apache.deltaspike.proxy.api.DeltaSpikeProxyFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Typed;
import javax.faces.component.PartialStateHolder;
import javax.faces.component.StateHolder;

@Typed
public class ConverterAndValidatorProxyFactory extends DeltaSpikeProxyFactory
{
    private static final ConverterAndValidatorProxyFactory INSTANCE = new ConverterAndValidatorProxyFactory();
    
    public static ConverterAndValidatorProxyFactory getInstance()
    {
        return INSTANCE;
    }

    @Override
    protected String getProxyClassSuffix()
    {
        return "$$DSJsfProxy";
    }

    @Override
    protected ArrayList<Method> getDelegateMethods(Class<?> targetClass, ArrayList<Method> allMethods)
    {
        if (!StateHolder.class.isAssignableFrom(targetClass))
        {
            ArrayList<Method> delegateMethods = new ArrayList<Method>();
            delegateMethods.addAll(Arrays.asList(StateHolder.class.getDeclaredMethods()));
            delegateMethods.addAll(Arrays.asList(PartialStateHolder.class.getDeclaredMethods()));
            return delegateMethods;
        }
        
        if (!PartialStateHolder.class.isAssignableFrom(targetClass))
        {
            ArrayList<Method> delegateMethods = new ArrayList<Method>();
            delegateMethods.addAll(Arrays.asList(PartialStateHolder.class.getDeclaredMethods()));
            return delegateMethods;
        }
        
        return null;
    }
    
    @Override
    protected Class<?>[] getAdditionalInterfacesToImplement(Class<?> targetClass)
    {
        List<Class<?>> interfaces = Arrays.asList(targetClass.getInterfaces());
        if (!interfaces.contains(PartialStateHolder.class))
        {
            return new Class<?>[] { PartialStateHolder.class };
        }
        
        return null;
    }
}
