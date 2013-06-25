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

import javax.persistence.Query;

import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.QueryResult;

public final class QueryProcessorFactory
{

    private final Method method;

    private QueryProcessorFactory(Method method)
    {
        this.method = method;
    }

    public static QueryProcessorFactory newInstance(Method method)
    {
        return new QueryProcessorFactory(method);
    }

    public QueryProcessor build()
    {
        if (returns(QueryResult.class))
        {
            return new NoOpQueryProcessor();
        }
        if (returns(List.class))
        {
            return new ListQueryProcessor();
        }
        if (isModifying())
        {
            return new ExecuteUpdateQueryProcessor(returns(Void.TYPE));
        }
        return new SingleResultQueryProcessor();
    }

    private boolean isModifying()
    {
        boolean matchesType = Void.TYPE.equals(method.getReturnType()) ||
                int.class.equals(method.getReturnType()) ||
                Integer.class.equals(method.getReturnType());
        return method.isAnnotationPresent(Modifying.class) && matchesType;
    }

    private boolean returns(Class<?> clazz)
    {
        return method.getReturnType().isAssignableFrom(clazz);
    }

    private static final class ListQueryProcessor implements QueryProcessor
    {
        @Override
        public Object executeQuery(Query query)
        {
            return query.getResultList();
        }
    }

    private static final class NoOpQueryProcessor implements QueryProcessor
    {
        @Override
        public Object executeQuery(Query query)
        {
            return query;
        }
    }

    private static final class SingleResultQueryProcessor implements QueryProcessor
    {
        @Override
        public Object executeQuery(Query query)
        {
            return query.getSingleResult();
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
        public Object executeQuery(Query query)
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
