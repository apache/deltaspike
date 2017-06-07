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

import java.util.List;
import javax.enterprise.context.ApplicationScoped;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.deltaspike.core.util.OptionalUtil;
import org.apache.deltaspike.core.util.StreamUtil;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.SingleResultType;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadata;

@ApplicationScoped
public class QueryProcessorFactory
{
    public QueryProcessor build(RepositoryMethodMetadata methodMetadata)
    {
        if (returns(methodMetadata, QueryResult.class))
        {
            return new NoOpQueryProcessor();
        }
        if (returns(methodMetadata, List.class))
        {
            return new ListQueryProcessor();
        }
        if (methodMetadata.isReturnsStream())
        {
            return new StreamQueryProcessor();
        }
        if (isModifying(methodMetadata))
        {
            return new ExecuteUpdateQueryProcessor(returns(methodMetadata, Void.TYPE));
        }
        return new SingleResultQueryProcessor();
    }

    private boolean isModifying(RepositoryMethodMetadata methodMetadata)
    {
        boolean matchesType = Void.TYPE.equals(methodMetadata.getMethod().getReturnType()) ||
                int.class.equals(methodMetadata.getMethod().getReturnType()) ||
                Integer.class.equals(methodMetadata.getMethod().getReturnType());
        return (methodMetadata.getMethod().isAnnotationPresent(Modifying.class) && matchesType)
                || methodMetadata.getMethodPrefix().isDelete();
    }

    private boolean returns(RepositoryMethodMetadata methodMetadata, Class<?> clazz)
    {
        return methodMetadata.getMethod().getReturnType().isAssignableFrom(clazz);
    }

    private static final class ListQueryProcessor implements QueryProcessor
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

    private static final class StreamQueryProcessor implements QueryProcessor
    {
        @Override
        public Object executeQuery(Query query, CdiQueryInvocationContext context)
        {
            return StreamUtil.wrap(query.getResultList());
        }
    }

    private static final class SingleResultQueryProcessor implements QueryProcessor
    {
        @Override
        public Object executeQuery(Query query, CdiQueryInvocationContext context)
        {
            SingleResultType style = context.getSingleResultStyle();
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
                return OptionalUtil.wrap(result);
            }
            else
            {
                return result;
            }
        }
    }

    private static final class ExecuteUpdateQueryProcessor implements QueryProcessor
    {

        private final boolean returnsVoid;

        private ExecuteUpdateQueryProcessor(boolean returnsVoid)
        {
            this.returnsVoid = returnsVoid;
        }

        @Override
        public Object executeQuery(Query query, CdiQueryInvocationContext context)
        {
            int result = query.executeUpdate();
            if (!returnsVoid)
            {
                return result;
            }
            return null;
        }
    }
}
