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
package org.apache.deltaspike.data.impl.tx;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.interceptor.InvocationContext;

import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;

public abstract class InvocationContextWrapper implements InvocationContext
{

    private final CdiQueryInvocationContext context;

    public InvocationContextWrapper(CdiQueryInvocationContext context)
    {
        this.context = context;
    }

    @Override
    public Map<String, Object> getContextData()
    {
        return new HashMap<String, Object>(0);
    }

    @Override
    public Method getMethod()
    {
        return context.getMethod();
    }

    @Override
    public Object[] getParameters()
    {
        return context.getMethodParameters();
    }

    @Override
    public Object getTarget()
    {
        return context.getProxy();
    }

    @Override
    public Object getTimer()
    {
        return null;
    }

    @Override
    public void setParameters(Object[] args)
    {
    }

}
