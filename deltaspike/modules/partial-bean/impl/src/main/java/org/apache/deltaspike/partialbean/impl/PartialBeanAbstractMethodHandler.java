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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Handler used for partial-beans which are abstract-classes.
 * At runtime it will be called from {@link MethodHandlerProxy} as instance of javassist.util.proxy.MethodHandler
 */
class PartialBeanAbstractMethodHandler<T extends InvocationHandler>
{
    private final T handlerInstance;

    PartialBeanAbstractMethodHandler(T handlerInstance)
    {
        this.handlerInstance = handlerInstance;
    }

    //Signature given by javassist.util.proxy.MethodHandler#invoke
    public Object invoke(Object target, Method method, Method proceedMethod, Object[] arguments) throws Throwable
    {
        if (proceedMethod != null)
        {
            return proceedMethod.invoke(target, arguments);
        }
        return this.handlerInstance.invoke(target, method, arguments);
    }
}
