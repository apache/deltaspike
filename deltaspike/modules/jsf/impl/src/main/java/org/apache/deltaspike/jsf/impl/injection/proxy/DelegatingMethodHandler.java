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

import javax.faces.FacesException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Handler delegates to implemented methods, if they exist
 */
class DelegatingMethodHandler<T extends InvocationHandler>
{
    private final T handlerInstance;

    DelegatingMethodHandler(T handlerInstance)
    {
        this.handlerInstance = handlerInstance;
    }

    //Signature given by javassist.util.proxy.MethodHandler#invoke
    public Object invoke(Object target, Method method, Method proceedMethod, Object[] arguments) throws Throwable
    {
        try
        {
            if (proceedMethod != null)
            {
                return proceedMethod.invoke(target, arguments);
            }
            return this.handlerInstance.invoke(target, method, arguments);
        }
        catch (InvocationTargetException e)
        {
            if (e.getCause() instanceof FacesException)
            {
                throw e.getCause();
            }
            throw e;
        }
    }
}
