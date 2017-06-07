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
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.QueryHint;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.api.provider.DependentProvider;

import org.apache.deltaspike.data.api.EntityGraph;
import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;
import org.apache.deltaspike.data.impl.graph.EntityGraphHelper;
import org.apache.deltaspike.data.impl.meta.EntityMetadata;
import org.apache.deltaspike.data.impl.meta.RepositoryMetadata;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadata;
import org.apache.deltaspike.data.impl.param.Parameters;
import org.apache.deltaspike.data.impl.property.Property;
import org.apache.deltaspike.data.impl.util.EntityUtils;
import org.apache.deltaspike.data.impl.util.bean.DependentProviderDestroyable;
import org.apache.deltaspike.data.impl.util.bean.Destroyable;
import org.apache.deltaspike.data.spi.QueryInvocationContext;

public class CdiQueryInvocationContext implements QueryInvocationContext
{

    private final EntityManager entityManager;
    private final Parameters params;
    private final Object proxy;
    private final Method method;
    private final Object[] args;
    
    private final RepositoryMetadata repositoryMetadata;
    private final RepositoryMethodMetadata repositoryMethodMetadata;
    
    private final List<QueryStringPostProcessor> queryPostProcessors;
    private final List<JpaQueryPostProcessor> jpaPostProcessors;
    private final List<Destroyable> cleanup;

    private String queryString;

    public CdiQueryInvocationContext(Object proxy, Method method, Object[] args,
            RepositoryMetadata repositoryMetadata,
            RepositoryMethodMetadata repositoryMethodMetadata, EntityManager entityManager)
    {
        this.proxy = proxy;
        this.method = method;
        this.args = args == null ? new Object[]{} : args;
        this.repositoryMetadata = repositoryMetadata;
        this.repositoryMethodMetadata = repositoryMethodMetadata;
        this.entityManager = entityManager;
        
        this.params = Parameters.create(method, this.args, repositoryMethodMetadata);
        this.queryPostProcessors = new LinkedList<QueryStringPostProcessor>();
        this.jpaPostProcessors = new LinkedList<JpaQueryPostProcessor>();
        this.cleanup = new LinkedList<Destroyable>();
    }

    public void initMapper()
    {
        if (hasQueryInOutMapper())
        {
            QueryInOutMapper<?> mapper = getQueryInOutMapper();
            params.applyMapper(mapper);
            for (int i = 0; i < args.length; i++)
            {
                if (mapper.mapsParameter(args[i]))
                {
                    args[i] = mapper.mapParameter(args[i]);
                }
            }
        }
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
            Property<Serializable> versionProperty = repositoryMetadata.getEntityMetadata().getVersionProperty();
            if (versionProperty != null)
            {
                return versionProperty.getValue(entity) == null;
            }

            Property<Serializable> primaryKeyProperty = repositoryMetadata.getEntityMetadata().getPrimaryKeyProperty();
            if (EntityUtils.primaryKeyValue(entity, primaryKeyProperty) == null)
            {
                return true;
            }

            if (!entityManager.contains(entity) && countCheck(entity, primaryKeyProperty))
            {
                return true;
            }

            return false;
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
        return repositoryMetadata.getEntityMetadata().getEntityClass();
    }

    @Override
    public Class<?> getRepositoryClass()
    {
        return repositoryMetadata.getRepositoryClass();
    }

    public Object proceed() throws Exception
    {
        return method.invoke(proxy, args);
    }

    @Override
    public Method getMethod()
    {
        return method;
    }

    public Query applyRestrictions(Query query)
    {
        Parameters params = getParams();
        Method method = getMethod();
        
        if (params.hasSizeRestriction())
        {
            query.setMaxResults(params.getSizeRestriciton());
        }
        
        if (params.hasFirstResult())
        {
            query.setFirstResult(params.getFirstResult());
        }
        
        LockModeType lockMode = extractLockMode();
        if (lockMode != null)
        {
            query.setLockMode(lockMode);
        }
        
        QueryHint[] hints = extractQueryHints();
        if (hints != null)
        {
            for (QueryHint hint : hints)
            {
                query.setHint(hint.name(), hint.value());
            }
        }

        applyEntityGraph(query, method);
        query = applyJpaQueryPostProcessors(query);
        return query;
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

    public void addDestroyable(Destroyable destroyable)
    {
        cleanup.add(destroyable);
    }

    public void cleanup()
    {
        for (Destroyable destroy : cleanup)
        {
            destroy.destroy();
        }
        cleanup.clear();
    }

    public Object executeQuery(Query jpaQuery)
    {
        return repositoryMethodMetadata.getQueryProcessor().executeQuery(jpaQuery, this);
    }

    public Parameters getParams()
    {
        return params;
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

    public boolean hasQueryInOutMapper()
    {
        return repositoryMethodMetadata.getQueryInOutMapperClass() != null;
    }

    public QueryInOutMapper<?> getQueryInOutMapper()
    {
        if (repositoryMethodMetadata.getQueryInOutMapperClass() == null)
        {
            return null;
        }

        QueryInOutMapper<?> result = null;
        if (repositoryMethodMetadata.isQueryInOutMapperIsNormalScope())
        {
            result = BeanProvider.getContextualReference(repositoryMethodMetadata.getQueryInOutMapperClass());
        }
        else
        {
            DependentProvider<? extends QueryInOutMapper<?>> mappedProvider =
                    BeanProvider.getDependent(repositoryMethodMetadata.getQueryInOutMapperClass());
            
            result = mappedProvider.get();
            
            this.addDestroyable(new DependentProviderDestroyable(mappedProvider));
        }
        
        return result;
    }

    public Object getProxy()
    {
        return proxy;
    }

    private LockModeType extractLockMode()
    {
        org.apache.deltaspike.data.api.Query query = getRepositoryMethodMetadata().getQuery();
        if (query != null && query.lock() != LockModeType.NONE)
        {
            return query.lock();
        }

        return null;
    }

    private QueryHint[] extractQueryHints()
    {
        org.apache.deltaspike.data.api.Query query = getRepositoryMethodMetadata().getQuery();        
        if (query != null && query.hints().length > 0)
        {
            return query.hints();
        }

        return null;
    }

    private void applyEntityGraph(Query query, Method method)
    {
        EntityGraph entityGraphAnn = method.getAnnotation(EntityGraph.class);
        if (entityGraphAnn == null)
        {
            return;
        }
        
        Object graph = EntityGraphHelper.getEntityGraph(getEntityManager(),
                repositoryMetadata.getEntityMetadata().getEntityClass(),
                entityGraphAnn);
        query.setHint(entityGraphAnn.type().getHintName(), graph);
    }

    private boolean countCheck(Object entity, Property<Serializable> primaryKeyProperty)
    {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(e) FROM " + getEntityClass()
                .getSimpleName() + " e ");
        jpql.append("WHERE e.");
        jpql.append(primaryKeyProperty.getName());
        jpql.append(" = :id");

        final Query query = entityManager.createQuery(jpql.toString());
        query.setParameter("id", EntityUtils.primaryKeyValue(entity, primaryKeyProperty));
        final Long result = (Long) query.getSingleResult();
        if (Long.valueOf(0).equals(result))
        {
            return true;
        }
        return false;
    }

    public RepositoryMetadata getRepositoryMetadata()
    {
        return repositoryMetadata;
    }

    public EntityMetadata getEntityMetadata()
    {
        return repositoryMetadata.getEntityMetadata();
    }
    
    public RepositoryMethodMetadata getRepositoryMethodMetadata()
    {
        return repositoryMethodMetadata;
    } 
}
