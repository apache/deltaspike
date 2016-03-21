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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.deltaspike.proxy.api.DeltaSpikeProxyFactory;

public class EnableInterceptorsProxyFactory extends DeltaSpikeProxyFactory
{
    private static final EnableInterceptorsProxyFactory INSTANCE = new EnableInterceptorsProxyFactory();
    
    public static EnableInterceptorsProxyFactory getInstance()
    {
        return INSTANCE;
    }
   
    @Override
    protected ArrayList<Method> getDelegateMethods(Class<?> targetClass, ArrayList<Method> allMethods)
    {
        ArrayList<Method> methods = new ArrayList<Method>();
        
        Iterator<Method> it = allMethods.iterator();
        while (it.hasNext())
        {
            Method method = it.next();

            if (Modifier.isPublic(method.getModifiers())
                    && !Modifier.isFinal(method.getModifiers())
                    && !Modifier.isAbstract(method.getModifiers()))
            {
                methods.add(method);
            }
        }
        
        return methods;
    }
    
    @Override
    protected ArrayList<Method> filterInterceptMethods(Class<?> targetClass, ArrayList<Method> allMethods)
    {
        return null;
    }

    @Override
    protected String getProxyClassSuffix()
    {
        return "$$DSInterceptorProxy";
    }
}
