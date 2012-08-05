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
package org.apache.deltaspike.cdise.weld;

import org.jboss.weld.context.beanstore.BeanStore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Method filter - needed to fix WELD-1072
 */
public class BeanStoreFilter implements InvocationHandler
{
    private final BeanStore wrapped;

    public BeanStoreFilter(BeanStore wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if ("clear".equals(method.getName()) && method.getParameterTypes().length == 0)
        {
            return null;
        }

        return method.invoke(this.wrapped, args);
    }
}
