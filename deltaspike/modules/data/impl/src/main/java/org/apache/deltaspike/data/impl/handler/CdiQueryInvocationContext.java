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

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.deltaspike.data.impl.meta.RepositoryMethod;
import org.apache.deltaspike.data.impl.param.Parameters;
import org.apache.deltaspike.data.spi.QueryInvocationContext;

public class CdiQueryInvocationContext implements QueryInvocationContext
{

    private final EntityManager entityManager;
    private final Parameters params;
    private final Class<?> entityClass;
    private final Object proxy;
    private final Method method;
    private final Object[] args;
    private final RepositoryMethod repoMethod;
    private final List<QueryStringPostProcessor> queryPostProcessors;
    private final List<JpaQueryPostProcessor> jpaPostProcessors;

    private String queryString;

    public CdiQueryInvocationContext(Object proxy, Method method, Object[] args, RepositoryMethod repoMethod,
            EntityManager entityManager)
    {
        this.entityManager = entityManager;
        this.args = args == null ? new Object[] {} : args;
        this.params = Parameters.create(method, this.args);
        this.proxy = proxy;
        this.method = method;
        this.repoMethod = repoMethod;
        this.entityClass = repoMethod.getRepository().getEntityClass();
        this.queryPostProcessors = new LinkedList<QueryStringPostProcessor>();
        this.jpaPostProcessors = new LinkedList<JpaQueryPostProcessor>();
    }

    @Override
    public EntityManager getEntityManager()
    {
        return entityManager;
    }

    @Override
    public boolean isNew(Object entity)
    {
        try
        {
            return entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity) == null;
        }
        catch (IllegalArgumentException e)
        {
            // Not an entity
            return false;
        }
    }

    @Override
    public Class<?> getEntityClass()
    {
        return entityClass;
    }

    public Object proceed() throws Exception
    {
        return method.invoke(proxy, args);
    }

    public Method getMethod()
    {
        return method;
    }

    public Object[] getMethodParameters()
    {
        return args;
    }

    public void addQueryStringPostProcessor(QueryStringPostProcessor postProcessor)
    {
        queryPostProcessors.add(postProcessor);
    }

    public void addJpaQueryPostProcessor(JpaQueryPostProcessor postProcessor)
    {
        jpaPostProcessors.add(postProcessor);
    }

    public void removeJpaQueryPostProcessor(JpaQueryPostProcessor postProcessor)
    {
        jpaPostProcessors.remove(postProcessor);
    }

    public boolean hasQueryStringPostProcessors()
    {
        return !queryPostProcessors.isEmpty();
    }

    public String applyQueryStringPostProcessors(String queryString)
    {
        String result = queryString;
        for (QueryStringPostProcessor processor : queryPostProcessors)
        {
            result = processor.postProcess(result);
        }
        return result;
    }

    public Query applyJpaQueryPostProcessors(Query query)
    {
        Query result = query;
        for (JpaQueryPostProcessor processor : jpaPostProcessors)
        {
            result = processor.postProcess(this, result);
        }
        return result;
    }

    public Object executeQuery(Query jpaQuery)
    {
        return repoMethod.getQueryProcessor().executeQuery(jpaQuery);
    }

    public Parameters getParams()
    {
        return params;
    }

    public RepositoryMethod getRepositoryMethod()
    {
        return repoMethod;
    }

    public String getQueryString()
    {
        return queryString;
    }

    public void setQueryString(String queryString)
    {
        this.queryString = queryString;
    }

    public List<QueryStringPostProcessor> getQueryStringPostProcessors()
    {
        return queryPostProcessors;
    }

}
