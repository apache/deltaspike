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
package org.apache.deltaspike.data.impl.builder.result;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.deltaspike.core.util.ClassUtils;

import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.QueryInvocationException;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.SingleResultType;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadata;

@ApplicationScoped
public class QueryProcessorFactory
{
    private NoOpQueryProcessor noOp;
    private ListResultQueryProcessor listResult;
    private StreamResultQueryProcessor streamResult;
    private ExecuteUpdateQueryProcessor executeUpdate;
    private SingleResultQueryProcessor singleResult;
    
    @PostConstruct
    public void init()
    {
        noOp = new NoOpQueryProcessor();
        listResult = new ListResultQueryProcessor();
        streamResult = new StreamResultQueryProcessor();
        executeUpdate = new ExecuteUpdateQueryProcessor();
        singleResult = new SingleResultQueryProcessor();
    }
    
    public QueryProcessor build(RepositoryMethodMetadata methodMetadata)
    {
        if (ClassUtils.returns(methodMetadata.getMethod(), QueryResult.class))
        {
            return noOp;
        }
        
        if (ClassUtils.returns(methodMetadata.getMethod(), List.class))
        {
            return listResult;
        }
        
        if (methodMetadata.isReturnsStream())
        {
            return streamResult;
        }
        
        if (isModifying(methodMetadata))
        {
            return executeUpdate;
        }

        return singleResult;
    }

    private boolean isModifying(RepositoryMethodMetadata methodMetadata)
    {
        boolean matchesType = Void.TYPE.equals(methodMetadata.getMethod().getReturnType()) ||
                int.class.equals(methodMetadata.getMethod().getReturnType()) ||
                Integer.class.equals(methodMetadata.getMethod().getReturnType());
        return (methodMetadata.getMethod().isAnnotationPresent(Modifying.class) && matchesType)
                || methodMetadata.getMethodPrefix().isDelete();
    }

    private static final class ListResultQueryProcessor implements QueryProcessor
    {
        @Override
        public Object executeQuery(Query query, CdiQueryInvocationContext context)
        {
            return query.getResultList();
        }
    }

    private static final class NoOpQueryProcessor implements QueryProcessor
    {
        @Override
        public Object executeQuery(Query query, CdiQueryInvocationContext context)
        {
            return query;
        }
    }

    private static final class StreamResultQueryProcessor implements QueryProcessor
    {
        // will be cached per @ApplicationScoped
        private boolean initialized;
        private Method getResultStreamMethod;
        
        @Override
        public Object executeQuery(Query query, CdiQueryInvocationContext context)
        {
            if (initialized == false)
            {
                initialized = true;
                try
                {
                    // take the query.getClass() instead of Query.class
                    // as the users might use JPA 2.2 API but still a JPA 2.0 impl (could happen in TomEE soon)
                    getResultStreamMethod = query.getClass().getMethod("getResultStream");
                }
                catch (Exception e)
                {
                    // ignore
                }
            }

            if (getResultStreamMethod != null)
            {
                try
                {
                    // delegate to JPA 2.2, which is probably optimized and fetches the data lazy
                    return getResultStreamMethod.invoke(query);
                }
                catch (Exception e)
                {
                    throw new QueryInvocationException(e, context);
                }
            }
            
            return query.getResultList().stream();
        }
    }

    private static final class SingleResultQueryProcessor implements QueryProcessor
    {
        @Override
        public Object executeQuery(Query query, CdiQueryInvocationContext context)
        {
            SingleResultType style = context.getRepositoryMethodMetadata().getSingleResultType();
            Object result = null;
            switch (style)
            {
                case JPA:
                    return query.getSingleResult();
                case OPTIONAL:
                    try
                    {
                        result = query.getSingleResult();
                    }
                    catch (NoResultException e)
                    {
                    }
                    break;
                default:
                    @SuppressWarnings("unchecked")
                    List<Object> queryResult = query.getResultList();
                    result = !queryResult.isEmpty() ? queryResult.get(0) : null;
            }
            
            if (context.getRepositoryMethodMetadata().isReturnsOptional())
            {
                return Optional.ofNullable(result);
            }
            else
            {
                return result;
            }
        }
    }

    private static final class ExecuteUpdateQueryProcessor implements QueryProcessor
    {
        @Override
        public Object executeQuery(Query query, CdiQueryInvocationContext context)
        {
            return query.executeUpdate();
        }
    }
}
