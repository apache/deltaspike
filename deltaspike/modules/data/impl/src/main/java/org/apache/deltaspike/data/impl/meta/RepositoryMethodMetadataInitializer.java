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

import static org.apache.deltaspike.core.util.StringUtils.isNotEmpty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.apache.deltaspike.core.util.OptionalUtil;
import org.apache.deltaspike.core.util.StreamUtil;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.mapping.MappingConfig;
import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;
import org.apache.deltaspike.data.impl.builder.MethodExpressionException;
import org.apache.deltaspike.data.impl.builder.part.QueryRoot;
import org.apache.deltaspike.data.impl.builder.result.QueryProcessorFactory;

@ApplicationScoped
public class RepositoryMethodMetadataInitializer
{
    @Inject
    private QueryProcessorFactory queryProcessorFactory;
    
    public RepositoryMethodMetadata init(RepositoryMetadata repositoryMetadata, Method method, BeanManager beanManager)
    {
        RepositoryMethodMetadata repositoryMethodMetadata = new RepositoryMethodMetadata();
        
        repositoryMethodMetadata.setMethod(method);

        repositoryMethodMetadata.setReturnsOptional(
                OptionalUtil.isOptionalReturned(method));
        repositoryMethodMetadata.setReturnsStream(
                StreamUtil.isStreamReturned(method));
        
        repositoryMethodMetadata.setQuery(method.isAnnotationPresent(Query.class)
                ? method.getAnnotation(Query.class) : null);
        repositoryMethodMetadata.setModifying(method.isAnnotationPresent(Modifying.class)
                ? method.getAnnotation(Modifying.class) : null);

        repositoryMethodMetadata.setMethodPrefix(new RepositoryMethodPrefix(
                    repositoryMetadata.getRepositoryClass().getAnnotation(Repository.class).methodPrefix(),
                    method.getName()));
        repositoryMethodMetadata.setMethodType(
                extractMethodType(repositoryMetadata, repositoryMethodMetadata));
        
        repositoryMethodMetadata.setQueryProcessor(queryProcessorFactory.build(repositoryMethodMetadata));
        
        repositoryMethodMetadata.setQueryInOutMapperClass(
                extractMapper(method, repositoryMetadata));

        initQueryRoot(repositoryMetadata, repositoryMethodMetadata);
        initQueryInOutMapperIsNormalScope(repositoryMetadata, repositoryMethodMetadata, beanManager);

        return repositoryMethodMetadata;
    }

    private RepositoryMethodType extractMethodType(RepositoryMetadata repositoryMetadata,
            RepositoryMethodMetadata repositoryMethodMetadata)
    {
        if (isAnnotated(repositoryMethodMetadata))
        {
            return RepositoryMethodType.ANNOTATED;
        }
        
        if (isMethodExpression(repositoryMetadata, repositoryMethodMetadata))
        {
            return RepositoryMethodType.PARSE;
        }
        
        return RepositoryMethodType.DELEGATE;
    }

    private void initQueryRoot(RepositoryMetadata repositoryMetadata, RepositoryMethodMetadata methodMetadata)
    {
        if (methodMetadata.getMethodType() == RepositoryMethodType.PARSE)
        {
            methodMetadata.setQueryRoot(
                    QueryRoot.create(methodMetadata.getMethod().getName(),
                            repositoryMetadata,
                            methodMetadata.getMethodPrefix()));
        }
        else
        {
            methodMetadata.setQueryRoot(QueryRoot.UNKNOWN_ROOT);
        }
    }
    
    private void initQueryInOutMapperIsNormalScope(RepositoryMetadata repositoryMetadata,
            RepositoryMethodMetadata repositoryMethodMetadata, BeanManager beanManager)
    {
        if (repositoryMethodMetadata.getQueryInOutMapperClass() != null)
        {
            Set<Bean<?>> beans = beanManager.getBeans(repositoryMethodMetadata.getQueryInOutMapperClass());
            Class<? extends Annotation> scope = beanManager.resolve(beans).getScope();
            repositoryMethodMetadata.setQueryInOutMapperIsNormalScope(beanManager.isNormalScope(scope));
        }
    }

    private boolean isAnnotated(RepositoryMethodMetadata repositoryMethodMetadata)
    {
        if (repositoryMethodMetadata.getQuery() != null)
        {
            return isValid(repositoryMethodMetadata.getQuery());
        }
        return false;
    }

    private boolean isValid(Query query)
    {
        return isNotEmpty(query.value()) || isNotEmpty(query.named());
    }

    private boolean isMethodExpression(RepositoryMetadata repositoryMetadata,
            RepositoryMethodMetadata repositoryMethodMetadata)
    {
        if (!Modifier.isAbstract(repositoryMethodMetadata.getMethod().getModifiers()))
        {
            return false;
        }
        
        try
        {
            QueryRoot.create(repositoryMethodMetadata.getMethod().getName(),
                    repositoryMetadata,
                    repositoryMethodMetadata.getMethodPrefix());
            return true;
        }
        catch (MethodExpressionException e)
        {
            return false;
        }
    }

    private Class<? extends QueryInOutMapper<?>> extractMapper(Method queryMethod,
            RepositoryMetadata repositoryMetadata)
    {
        if (queryMethod.isAnnotationPresent(MappingConfig.class))
        {
            return queryMethod.getAnnotation(MappingConfig.class).value();
        }
        
        if (repositoryMetadata.getRepositoryClass().isAnnotationPresent(MappingConfig.class))
        {
            return repositoryMetadata.getRepositoryClass().getAnnotation(MappingConfig.class).value();
        }
        
        return null;
    }
}
