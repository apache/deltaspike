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
import java.util.HashMap;
import java.util.Map;
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
import org.apache.deltaspike.data.impl.meta.RepositoryMethodType;
import org.apache.deltaspike.data.impl.meta.QueryInvocation;
import org.apache.deltaspike.data.impl.util.bean.BeanDestroyable;
import org.apache.deltaspike.data.spi.DelegateQueryHandler;

@QueryInvocation(RepositoryMethodType.DELEGATE)
@ApplicationScoped
public class DelegateQueryBuilder extends QueryBuilder
{
    @Inject
    private BeanManager beanManager;

    private final Map<Method, Bean<DelegateQueryHandler>> lookupCache
        = new HashMap<Method, Bean<DelegateQueryHandler>>();
    
    @Override
    public Object execute(CdiQueryInvocationContext context)
    {
        try
        {
            DelegateQueryHandler delegate = lookup(context);
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

    private DelegateQueryHandler lookup(CdiQueryInvocationContext context)
    {
        Bean<DelegateQueryHandler> selectedBean = lookupCache.get(context.getMethod());
        
        if (selectedBean == null)
        {
            Set<Bean<DelegateQueryHandler>> beans = BeanProvider
                    .getBeanDefinitions(DelegateQueryHandler.class, true, true);
            for (Bean<DelegateQueryHandler> bean : beans)
            {
                if (ClassUtils.containsPossiblyGenericMethod(bean.getBeanClass(), context.getMethod()))
                {
                    selectedBean = bean;
                }
            }
            
            if (selectedBean != null)
            {
                lookupCache.put(context.getMethod(), selectedBean);
            }
        }
        
        
        if (selectedBean != null)
        {
            CreationalContext<DelegateQueryHandler> cc = beanManager.createCreationalContext(selectedBean);
            DelegateQueryHandler instance = (DelegateQueryHandler) beanManager.getReference(
                    selectedBean, DelegateQueryHandler.class, cc);
            
            if (selectedBean.getScope().equals(Dependent.class))
            {
                context.addDestroyable(new BeanDestroyable<DelegateQueryHandler>(selectedBean, instance, cc));
            }

            return instance;
        }
        return null;
    }

    private Object invoke(DelegateQueryHandler delegate, CdiQueryInvocationContext context)
    {
        try
        {
            Method extract = ClassUtils.extractPossiblyGenericMethod(delegate.getClass(), context.getMethod());
            return extract.invoke(delegate, context.getMethodParameters());
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
}
