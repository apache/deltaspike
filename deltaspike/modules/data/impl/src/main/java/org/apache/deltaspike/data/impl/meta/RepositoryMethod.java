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
package org.apache.deltaspike.data.impl.meta;

import static org.apache.deltaspike.data.impl.util.QueryUtils.isNotEmpty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.api.provider.DependentProvider;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.SingleResultType;
import org.apache.deltaspike.data.api.mapping.MappingConfig;
import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;
import org.apache.deltaspike.data.impl.builder.MethodExpressionException;
import org.apache.deltaspike.data.impl.builder.part.QueryRoot;
import org.apache.deltaspike.data.impl.builder.result.QueryProcessor;
import org.apache.deltaspike.data.impl.builder.result.QueryProcessorFactory;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.util.bean.DependentProviderDestroyable;

/**
 * Stores information about a specific method of a Repository:
 * <ul>
 * <li>The reference to the Method reflection object</li>
 * <li>Whether this method delegates, is annotated or is parsed</li>
 * <li>A reference to the parent Repository</li>
 * <li>For parsed Repository methods, also the JPQL string is cached</li>
 * </ul>
 */
public class RepositoryMethod
{

    private final Method method;
    private final MethodType methodType;
    private final MethodPrefix methodPrefix;
    private final RepositoryComponent repo;
    private final QueryRoot queryRoot;
    private final QueryProcessor queryProcessor;
    private final Class<? extends QueryInOutMapper> mapper;

    private volatile Boolean queryInOutMapperIsNormalScope;

    public RepositoryMethod(Method method, RepositoryComponent repo)
    {
        this.method = method;
        this.repo = repo;
        this.methodPrefix = new MethodPrefix(repo.getCustomMethodPrefix(), method.getName());
        this.methodType = extractMethodType();
        this.queryRoot = initQueryRoot();
        this.queryProcessor = QueryProcessorFactory.newInstance(method).build();
        this.mapper = extractMapper(method, repo);
    }

    public boolean returns(Class<?> returnType)
    {
        return returnType.equals(method.getReturnType());
    }

    public QueryInOutMapper<?> getQueryInOutMapperInstance(CdiQueryInvocationContext context)
    {
        if (!hasQueryInOutMapper())
        {
            return null;
        }
        QueryInOutMapper<?> result = null;
        lazyInit();
        if (!queryInOutMapperIsNormalScope)
        {
            final DependentProvider<? extends QueryInOutMapper> mappedProvider = BeanProvider.getDependent(mapper);
            result = mappedProvider.get();
            context.addDestroyable(new DependentProviderDestroyable(mappedProvider));
        }
        else
        {
            result = BeanProvider.getContextualReference(mapper);
        }
        return result;
    }

    private MethodType extractMethodType()
    {
        if (isAnnotated())
        {
            return MethodType.ANNOTATED;
        }
        if (isMethodExpression())
        {
            return MethodType.PARSE;
        }
        return MethodType.DELEGATE;
    }

    private QueryRoot initQueryRoot()
    {
        if (methodType == MethodType.PARSE)
        {
            return QueryRoot.create(method.getName(), repo, methodPrefix);
        }
        return QueryRoot.UNKNOWN_ROOT;
    }

    private boolean isAnnotated()
    {
        if (method.isAnnotationPresent(Query.class))
        {
            Query query = method.getAnnotation(Query.class);
            return isValid(query);
        }
        return false;
    }

    private boolean isValid(Query query)
    {
        return isNotEmpty(query.value()) || isNotEmpty(query.named());
    }

    private boolean isMethodExpression()
    {
        if (!Modifier.isAbstract(method.getModifiers()))
        {
            return false;
        }
        try
        {
            QueryRoot.create(method.getName(), repo, methodPrefix);
            return true;
        }
        catch (MethodExpressionException e)
        {
            return false;
        }
    }

    private Class<? extends QueryInOutMapper> extractMapper(Method queryMethod, RepositoryComponent repoComponent)
    {
        if (queryMethod.isAnnotationPresent(MappingConfig.class))
        {
            return queryMethod.getAnnotation(MappingConfig.class).value();
        }
        if (repoComponent.getRepositoryClass().isAnnotationPresent(MappingConfig.class))
        {
            return repoComponent.getRepositoryClass().getAnnotation(MappingConfig.class).value();
        }
        return null;
    }

    //don't trigger this lookup during ProcessAnnotatedType
    private void lazyInit()
    {
        if (queryInOutMapperIsNormalScope == null)
        {
            init(BeanManagerProvider.getInstance().getBeanManager());
        }
    }

    private synchronized void init(BeanManager beanManager)
    {
        if (queryInOutMapperIsNormalScope != null)
        {
            return;
        }

        if (queryInOutMapperIsNormalScope != null && beanManager != null)
        {
            final Set<Bean<?>> beans = beanManager.getBeans(mapper);
            final Class<? extends Annotation> scope = beanManager.resolve(beans).getScope();
            queryInOutMapperIsNormalScope = beanManager.isNormalScope(scope);
        }
        else
        {
            queryInOutMapperIsNormalScope = false;
        }
    }

    public MethodType getMethodType()
    {
        return methodType;
    }

    public RepositoryComponent getRepository()
    {
        return repo;
    }

    public QueryRoot getQueryRoot()
    {
        return queryRoot;
    }

    public QueryProcessor getQueryProcessor()
    {
        return queryProcessor;
    }

    public boolean hasQueryInOutMapper()
    {
        return mapper != null;
    }

    public SingleResultType getSingleResultStyle()
    {
        if (method.isAnnotationPresent(Query.class))
        {
            return method.getAnnotation(Query.class).singleResult();
        }
        return methodPrefix.getSingleResultStyle();
    }

}
