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
package org.apache.deltaspike.core.impl.invocationhandler;

import javassist.util.proxy.MethodHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class PartialBeanMethodHandler<T extends InvocationHandler> implements MethodHandler
{
    private final T handlerInstance;

    PartialBeanMethodHandler(T handlerInstance)
    {
        this.handlerInstance = handlerInstance;
    }

    public Object invoke(Object target, Method method, Method proceedMethod, Object[] arguments) throws Throwable
    {
        if (proceedMethod != null)
        {
            return proceedMethod.invoke(target, arguments);
        }
        return this.handlerInstance.invoke(target, method, arguments);
    }

    T getHandlerInstance()
    {
        return this.handlerInstance;
    }
}
