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
package org.apache.deltaspike.core.util.interceptor;

import javax.enterprise.inject.Typed;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Typed()
public abstract class AbstractInvocationContext<T> implements InvocationContext
{
    protected final T target;
    protected final Method method;
    protected final Object timer;

    protected Object[] parameters;
    protected Map<String, Object> contextData;

    protected AbstractInvocationContext(T target, Method method, Object[] parameters, Object timer)
    {
        this.target = target;
        this.method = method;
        this.parameters = parameters;
        this.timer = timer;
    }


    @Override
    public Object getTarget()
    {
        return target;
    }

    @Override
    public Method getMethod()
    {
        return method;
    }

    @Override
    public Object getTimer()
    {
        return timer;
    }

    @Override
    public Object[] getParameters()
    {
        return parameters;
    }

    @Override
    public void setParameters(Object[] parameters)
    {
        this.parameters = parameters;
    }

    @Override
    public Map<String, Object> getContextData()
    {
        if (contextData == null)
        {
            contextData = new HashMap<String, Object>();
        }
        return contextData;
    }

    // @Override - forward compatibility to interceptors API 1.2
    public Constructor<?> getConstructor()
    {
        return null;
    }
}
