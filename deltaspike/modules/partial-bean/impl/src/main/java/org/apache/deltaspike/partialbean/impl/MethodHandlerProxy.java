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

//This indirection to create a proxy for javassist.util.proxy.MethodHandler is used as intermediate approach.
//Further details see comments in PartialBeanLifecycle
public class MethodHandlerProxy implements InvocationHandler
{
    private PartialBeanAbstractMethodHandler partialBeanMethodHandler;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        //hardcoding the following parameters is ok since MethodHandlerProxy is only used for
        //javassist.util.proxy.MethodHandler which has one method with those parameters.
        return partialBeanMethodHandler.invoke(args[0], (Method)args[1], (Method)args[2], (Object[])args[3]);
    }

    void setPartialBeanMethodHandler(PartialBeanAbstractMethodHandler partialBeanMethodHandler)
    {
        this.partialBeanMethodHandler = partialBeanMethodHandler;
    }
}
