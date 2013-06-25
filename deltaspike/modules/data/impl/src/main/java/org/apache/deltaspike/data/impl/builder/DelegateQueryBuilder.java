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
package org.apache.deltaspike.data.impl.builder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.handler.QueryInvocationException;
import org.apache.deltaspike.data.impl.meta.MethodType;
import org.apache.deltaspike.data.impl.meta.QueryInvocation;
import org.apache.deltaspike.data.spi.DelegateQueryHandler;

@QueryInvocation(MethodType.DELEGATE)
public class DelegateQueryBuilder extends QueryBuilder
{

    @Inject
    @Any
    private Instance<DelegateQueryHandler> delegates;

    @Override
    public Object execute(CdiQueryInvocationContext context)
    {
        try
        {
            DelegateQueryHandler delegate = selectDelegate(context.getMethod());
            if (delegate != null)
            {
                return invoke(delegate, context);
            }
        }
        catch (Exception e)
        {
            throw new QueryInvocationException(e, context);
        }
        throw new QueryInvocationException("No DelegateQueryHandler found", context);
    }

    private DelegateQueryHandler selectDelegate(Method method)
    {
        for (DelegateQueryHandler delegate : delegates)
        {
            if (contains(delegate, method))
            {
                return delegate;
            }
        }
        return null;
    }

    private boolean contains(Object obj, Method method)
    {
        return extract(obj, method) != null;
    }

    private Method extract(Object obj, Method method)
    {
        try
        {
            String name = method.getName();
            return obj != null ? obj.getClass().getMethod(name, method.getParameterTypes()) : null;
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
    }

    private Object invoke(DelegateQueryHandler delegate, CdiQueryInvocationContext context)
    {
        try
        {
            return invoke(delegate, context.getMethod(), context.getMethodParameters());
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected Object invoke(Object target, Method method, Object[] args) throws InvocationTargetException,
            IllegalAccessException
    {
        Method extract = extract(target, method);
        return extract.invoke(target, args);
    }

}
