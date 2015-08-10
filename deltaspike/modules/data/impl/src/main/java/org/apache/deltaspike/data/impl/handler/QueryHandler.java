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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.apache.deltaspike.core.api.lifecycle.Initialized;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.apache.deltaspike.data.api.QueryInvocationException;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.builder.QueryBuilderFactory;
import org.apache.deltaspike.data.impl.meta.RepositoryComponent;
import org.apache.deltaspike.data.impl.meta.RepositoryComponents;
import org.apache.deltaspike.data.impl.meta.RepositoryMethod;

/**
 * Entry point for query processing.
 */
@Repository
public class QueryHandler implements Serializable, InvocationHandler
{

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(QueryHandler.class.getName());

    @Inject
    private QueryBuilderFactory queryBuilder;

    @Inject
    @Initialized
    private RepositoryComponents components;

    @Inject
    private CdiQueryContextHolder context;

    @Inject
    private EntityManagerLookup entityManagerLookup;

    @Inject
    private QueryRunner runner;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        CdiQueryInvocationContext queryContext = null;
        try
        {
            List<Class<?>> candidates = ProxyUtils.getProxyAndBaseTypes(proxy.getClass());
            RepositoryComponent repo = components.lookupComponent(candidates);
            RepositoryMethod repoMethod = components.lookupMethod(repo.getRepositoryClass(), method);
            queryContext = createContext(proxy, method, args, repo, repoMethod);
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
            entityManagerLookup.release();
            context.dispose();
        }
    }

    private CdiQueryInvocationContext createContext(Object proxy, Method method,
            Object[] args, RepositoryComponent repo, RepositoryMethod repoMethod)
    {
        CdiQueryInvocationContext queryContext = new CdiQueryInvocationContext(proxy, method, args, repoMethod,
                entityManagerLookup.lookupFor(repo));
        context.set(queryContext);
        queryContext.initMapper();
        return queryContext;
    }

}
