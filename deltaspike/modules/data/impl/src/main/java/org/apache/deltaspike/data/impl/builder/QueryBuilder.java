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
package org.apache.deltaspike.data.impl.builder;

import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;

import javax.persistence.Query;
import java.text.MessageFormat;
import java.util.List;

/**
 * Query builder factory. Delegates to concrete implementations.
 */
public abstract class QueryBuilder
{
    public static final String QUERY_SELECT = "select e from {0} e";
    public static final String QUERY_COUNT = "select count(e) from {0} e";
    public static final String QUERY_DELETE = "delete from {0} e";
    public static final String ENTITY_NAME = "e";

    public static String selectQuery(String entityName)
    {
        return MessageFormat.format(QUERY_SELECT, entityName);
    }

    public static String deleteQuery(String entityName)
    {
        return MessageFormat.format(QUERY_DELETE, entityName);
    }

    public static String countQuery(String entityName)
    {
        return MessageFormat.format(QUERY_COUNT, entityName);
    }

    @SuppressWarnings("unchecked")
    public Object executeQuery(CdiQueryInvocationContext context)
    {
        Object result = execute(context);
        if (!isUnmappableResult(result) && context.hasQueryInOutMapper())
        {
            QueryInOutMapper<Object> mapper = (QueryInOutMapper<Object>)
                    context.getQueryInOutMapper();
            if (result instanceof List)
            {
                return mapper.mapResultList((List<Object>) result);
            }
            return mapper.mapResult(result);
        }
        return result;
    }

    protected abstract Object execute(CdiQueryInvocationContext ctx);

    private boolean isUnmappableResult(Object result)
    {
        return result instanceof QueryResult ||
                result instanceof Query;
    }

}
