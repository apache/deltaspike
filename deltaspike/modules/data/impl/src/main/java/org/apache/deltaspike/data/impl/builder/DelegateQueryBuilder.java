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
import java.util.Collection;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.OptionalUtil;
import org.apache.deltaspike.core.util.StreamUtil;
import org.apache.deltaspike.data.api.QueryInvocationException;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.meta.MethodType;
import org.apache.deltaspike.data.impl.meta.QueryInvocation;
import org.apache.deltaspike.data.impl.util.bean.BeanDestroyable;
import org.apache.deltaspike.data.spi.DelegateQueryHandler;

@QueryInvocation(MethodType.DELEGATE)
@ApplicationScoped
public class DelegateQueryBuilder extends QueryBuilder
{
    @Inject
    private BeanManager beanManager;

    @Override
    public Object execute(CdiQueryInvocationContext context)
    {
        try
        {
            DelegateQueryHandler delegate = selectDelegate(context);
            if (delegate != null)
            {
                Object result = invoke(delegate, context);
                if (result instanceof Collection && StreamUtil.isStreamReturned(context.getMethod()))
                {
                    return StreamUtil.wrap(result);
                }
                else if (OptionalUtil.isOptionalReturned(context.getMethod()))
                {
                    return OptionalUtil.wrap(result);
                }
                else
                {
                    return result;
                }
            }
        }
        catch (PersistenceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new QueryInvocationException(e, context);
        }
        throw new QueryInvocationException("No DelegateQueryHandler found", context);
    }

    private DelegateQueryHandler selectDelegate(CdiQueryInvocationContext context)
    {
        Set<Bean<DelegateQueryHandler>> beans = BeanProvider
                .getBeanDefinitions(DelegateQueryHandler.class, true, true);
        for (Bean<DelegateQueryHandler> bean : beans)
        {
            if (ClassUtils.containsPossiblyGenericMethod(bean.getBeanClass(), context.getMethod()))
            {
                if (bean.getScope().equals(Dependent.class))
                {
                    CreationalContext<DelegateQueryHandler> cc = beanManager.createCreationalContext(bean);
                    DelegateQueryHandler instance = (DelegateQueryHandler) beanManager.getReference(
                            bean, DelegateQueryHandler.class, cc);
                    context.addDestroyable(new BeanDestroyable<DelegateQueryHandler>(bean, instance, cc));
                    return instance;
                }
                return (DelegateQueryHandler) BeanProvider.getContextualReference(bean.getBeanClass());
            }
        }
        return null;
    }

    private Object invoke(DelegateQueryHandler delegate, CdiQueryInvocationContext context)
    {
        try
        {
            return invoke(delegate, context.getMethod(), context.getMethodParameters());
        }
        catch (InvocationTargetException e)
        {
            if (e.getCause() != null && e.getCause() instanceof PersistenceException)
            {
                throw (PersistenceException) e.getCause();
            }
            throw new QueryInvocationException(e, context);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected Object invoke(Object target, Method method, Object[] args) throws InvocationTargetException,
            IllegalAccessException
    {
        Method extract = ClassUtils.extractPossiblyGenericMethod(target.getClass(), method);
        return extract.invoke(target, args);
    }

}
