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

import org.apache.deltaspike.core.api.lifecycle.Initialized;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.AnnotationUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.apache.deltaspike.core.util.interceptor.AbstractInvocationContext;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.data.api.QueryInvocationException;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.builder.QueryBuilderFactory;
import org.apache.deltaspike.data.impl.meta.RepositoryComponent;
import org.apache.deltaspike.data.impl.meta.RepositoryComponents;
import org.apache.deltaspike.data.impl.meta.RepositoryMethod;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.apache.deltaspike.jpa.spi.entitymanager.ActiveEntityManagerHolder;
import org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;

/**
 * Entry point for query processing.
 */
@Repository
@ApplicationScoped
public class QueryHandler implements Serializable, InvocationHandler
{
    private static final Logger log = Logger.getLogger(QueryHandler.class.getName());

    @Inject
    private QueryBuilderFactory queryBuilder;

    @Inject
    @Initialized
    private RepositoryComponents components;

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
        Transactional transactionalAnnotation =
            AnnotationUtils.extractAnnotationFromMethodOrClass(
                this.beanManager, method, proxy.getClass(), Transactional.class);

        if (transactionalAnnotation != null)
        {
            if (transactionalAnnotation.qualifier().length > 1)
            {
                throw new IllegalStateException(proxy.getClass().getName() + " uses @" + Transactional.class.getName() +
                    " with multiple qualifiers. That isn't supported with @" + Repository.class.getName());
            }

            Class<? extends Annotation> qualifier = transactionalAnnotation.qualifier()[0];
            if (!Any.class.equals(qualifier))
            {
                EntityManager entityManager = BeanProvider.getContextualReference(
                    EntityManager.class, false, AnnotationInstanceProvider.of(qualifier));
                activeEntityManagerHolder.set(entityManager);
            }

            return transactionStrategy.execute(
                new AbstractInvocationContext<Object>(proxy, method, args, null)
                {
                    @Override
                    public Object proceed() throws Exception
                    {
                        try
                        {
                            return process(proxy, method, args);
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
            return process(proxy, method, args);
        }
    }

    public Object process(Object proxy, Method method, Object[] args) throws Throwable
    {
        CdiQueryInvocationContext queryContext = null;
        EntityManagerRef entityManagerRef = null;
        try
        {
            List<Class<?>> candidates = ProxyUtils.getProxyAndBaseTypes(proxy.getClass());
            RepositoryComponent repo = components.lookupComponent(candidates);
            RepositoryMethod repoMethod = components.lookupMethod(repo.getRepositoryClass(), method);

            entityManagerRef = entityManagerRefLookup.lookupReference(repo);
            queryContext = createContext(proxy, method, args, entityManagerRef.getEntityManager(), repoMethod);
            
            QueryBuilder builder = queryBuilder.build(repoMethod, queryContext);
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
            if (entityManagerRef != null && entityManagerRef.getEntityManagerResolverDependentProvider() != null)
            {
                entityManagerRef.getEntityManagerResolverDependentProvider().destroy();
            }
            context.dispose();
        }
    }

    private CdiQueryInvocationContext createContext(Object proxy, Method method,
            Object[] args, EntityManager entityManager, RepositoryMethod repoMethod)
    {
        CdiQueryInvocationContext queryContext = new CdiQueryInvocationContext(proxy, method, args, repoMethod,
                entityManager);
        context.set(queryContext);
        queryContext.initMapper();
        return queryContext;
    }

}
