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
package org.apache.deltaspike.data.impl.handler;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.apache.deltaspike.core.util.interceptor.AbstractInvocationContext;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.data.api.QueryInvocationException;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.builder.QueryBuilderFactory;
import org.apache.deltaspike.data.impl.meta.RepositoryMetadataHandler;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.apache.deltaspike.jpa.impl.entitymanager.EntityManagerRef;
import org.apache.deltaspike.jpa.impl.entitymanager.EntityManagerRefLookup;
import org.apache.deltaspike.jpa.spi.entitymanager.ActiveEntityManagerHolder;
import org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.data.impl.meta.RepositoryMetadata;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadata;

/**
 * Entry point for query processing.
 */
@Repository
@ApplicationScoped
public class QueryHandler implements Serializable, InvocationHandler
{
    private static final Logger log = Logger.getLogger(QueryHandler.class.getName());

    @Inject
    private QueryBuilderFactory queryBuilderFactory;

    @Inject
    private RepositoryMetadataHandler metadataHandler;

    @Inject
    private CdiQueryContextHolder context;

    @Inject
    private EntityManagerRefLookup entityManagerRefLookup;

    @Inject
    private QueryRunner runner;

    @Inject
    private BeanManager beanManager;

    @Inject
    private TransactionStrategy transactionStrategy;

    @Inject
    private ActiveEntityManagerHolder activeEntityManagerHolder;

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
    {
        List<Class<?>> candidates = ProxyUtils.getProxyAndBaseTypes(proxy.getClass());
        final RepositoryMetadata repositoryMetadata =
                metadataHandler.lookupMetadata(candidates);
        final RepositoryMethodMetadata repositoryMethodMetadata =
                metadataHandler.lookupMethodMetadata(repositoryMetadata, method);

        if (repositoryMethodMetadata.getTransactional() != null)
        {
            if (repositoryMethodMetadata.getTransactional().qualifier().length > 1)
            {
                throw new IllegalStateException(proxy.getClass().getName() + " uses @" + Transactional.class.getName() +
                    " with multiple qualifiers. That isn't supported with @" + Repository.class.getName());
            }

            Class<? extends Annotation> qualifier = repositoryMethodMetadata.getTransactional().qualifier()[0];
            if (!Any.class.equals(qualifier))
            {
                EntityManager entityManager = BeanProvider.getContextualReference(
                    EntityManager.class, false, AnnotationInstanceProvider.of(qualifier));
                activeEntityManagerHolder.set(entityManager);
            }

            return transactionStrategy.execute(
                new AbstractInvocationContext<>(proxy, method, args, null)
                {
                    @Override
                    public Object proceed() throws Exception
                    {
                        try
                        {
                            return process(proxy, method, args, repositoryMetadata, repositoryMethodMetadata);
                        }
                        catch (Throwable t)
                        {
                            throw ExceptionUtils.throwAsRuntimeException(t);
                        }
                    }
                });
        }
        else
        {
            return process(proxy, method, args, repositoryMetadata, repositoryMethodMetadata);
        }
    }

    protected Object process(Object proxy, Method method, Object[] args,
            RepositoryMetadata repositoryMetadata, RepositoryMethodMetadata repositoryMethodMetadata) throws Throwable
    {
        CdiQueryInvocationContext queryContext = null;
        EntityManagerRef entityManagerRef = null;
        try
        {
            entityManagerRef = entityManagerRefLookup.lookupReference(repositoryMetadata);
            EntityManager entityManager = entityManagerRef.getEntityManager();
            if (entityManager == null)
            {
                throw new IllegalStateException("Unable to look up EntityManager");
            }
            queryContext = createContext(proxy, method, args, entityManager,
                    repositoryMetadata, repositoryMethodMetadata);
            
            QueryBuilder builder = queryBuilderFactory.build(repositoryMethodMetadata, queryContext);
            Object result = runner.executeQuery(builder, queryContext);
            return result;
        }
        catch (PersistenceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.log(Level.FINEST, "Query execution error", e);
            if (queryContext != null)
            {
                throw new QueryInvocationException(e, queryContext);
            }
            throw new QueryInvocationException(e, proxy.getClass(), method);
        }
        finally
        {
            if (entityManagerRef != null)
            {
                entityManagerRef.release();
            }
            context.dispose();
        }
    }

    private CdiQueryInvocationContext createContext(Object proxy, Method method,
            Object[] args, EntityManager entityManager, RepositoryMetadata repositoryMetadata,
            RepositoryMethodMetadata repositoryMethodMetadata)
    {
        CdiQueryInvocationContext queryContext = new CdiQueryInvocationContext(proxy, method, args,
                repositoryMetadata, repositoryMethodMetadata, entityManager);
        context.set(queryContext);
        queryContext.init();
        return queryContext;
    }

}
