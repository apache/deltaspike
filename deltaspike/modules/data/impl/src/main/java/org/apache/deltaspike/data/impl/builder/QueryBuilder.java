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

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;

import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.QueryHint;

import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.param.Parameters;

/**
 * Query builder factory. Delegates to concrete implementations.
 *
 * @author thomashug
 */
public abstract class QueryBuilder
{

    public static final String QUERY_SELECT = "select e from {0} e";
    public static final String QUERY_COUNT = "select count(e) from {0} e";
    public static final String ENTITY_NAME = "e";

    public static String selectQuery(String entityName)
    {
        return MessageFormat.format(QUERY_SELECT, entityName);
    }

    public static String countQuery(String entityName)
    {
        return MessageFormat.format(QUERY_COUNT, entityName);
    }

    public abstract Object execute(CdiQueryInvocationContext ctx);

    protected boolean returnsList(Method method)
    {
        return method.getReturnType().isAssignableFrom(List.class);
    }

    protected LockModeType extractLockMode(Method method)
    {
        Class<org.apache.deltaspike.data.api.Query> query = org.apache.deltaspike.data.api.Query.class;
        if (method.isAnnotationPresent(query) &&
                method.getAnnotation(query).lock() != LockModeType.NONE)
        {
            return method.getAnnotation(query).lock();
        }
        return null;
    }

    protected boolean hasLockMode(Method method)
    {
        return extractLockMode(method) != null;
    }

    protected QueryHint[] extractQueryHints(Method method)
    {
        Class<org.apache.deltaspike.data.api.Query> query = org.apache.deltaspike.data.api.Query.class;
        if (method.isAnnotationPresent(query) &&
                method.getAnnotation(query).hints().length > 0)
        {
            return method.getAnnotation(query).hints();
        }
        return null;
    }

    protected boolean hasQueryHints(Method method)
    {
        return extractQueryHints(method) != null;
    }

    protected Query applyRestrictions(CdiQueryInvocationContext context, Query query)
    {
        Parameters params = context.getParams();
        Method method = context.getMethod();
        if (params.hasSizeRestriction())
        {
            query.setMaxResults(params.getSizeRestriciton());
        }
        if (params.hasFirstResult())
        {
            query.setFirstResult(params.getFirstResult());
        }
        if (hasLockMode(method))
        {
            query.setLockMode(extractLockMode(method));
        }
        if (hasQueryHints(method))
        {
            QueryHint[] hints = extractQueryHints(method);
            for (QueryHint hint : hints)
            {
                query.setHint(hint.name(), hint.value());
            }
        }
        query = context.applyJpaQueryPostProcessors(query);
        return query;
    }

}
