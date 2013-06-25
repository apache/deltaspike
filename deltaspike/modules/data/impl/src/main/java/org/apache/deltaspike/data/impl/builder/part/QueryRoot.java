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
package org.apache.deltaspike.data.impl.builder.part;

import static org.apache.deltaspike.data.impl.util.QueryUtils.splitByKeyword;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.deltaspike.data.impl.builder.MethodExpressionException;
import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.builder.QueryBuilderContext;
import org.apache.deltaspike.data.impl.meta.RepositoryComponent;

/**
 * Root of the query tree. Also the only exposed class in the package.
 *
 * @author thomashug
 */
public class QueryRoot extends QueryPart
{

    public static final QueryRoot UNKNOWN_ROOT = new QueryRoot("null-object", "");

    private static final Logger log = Logger.getLogger(QueryRoot.class.getName());

    private final String entityName;
    private final String queryPrefix;

    private String jpqlQuery;

    protected QueryRoot(String entityName, String queryPrefix)
    {
        this.entityName = entityName;
        this.queryPrefix = queryPrefix;
    }

    public static QueryRoot create(String method, RepositoryComponent repo)
    {
        QueryRoot root = new QueryRoot(repo.getEntityName(), repo.getMethodPrefix());
        root.build(method, method, repo);
        root.createJpql();
        return root;
    }

    public String getJpqlQuery()
    {
        return jpqlQuery;
    }

    @Override
    protected QueryPart build(String queryPart, String method, RepositoryComponent repo)
    {
        String[] orderByParts = splitByKeyword(queryPart, "OrderBy");
        if (hasQueryConditions(orderByParts))
        {
            String[] orParts = splitByKeyword(removePrefix(orderByParts[0]), "Or");
            boolean first = true;
            for (String or : orParts)
            {
                OrQueryPart orPart = new OrQueryPart(first);
                first = false;
                children.add(orPart.build(or, method, repo));
            }
        }
        if (orderByParts.length > 1)
        {
            OrderByQueryPart orderByPart = new OrderByQueryPart();
            children.add(orderByPart.build(orderByParts[1], method, repo));
        }
        if (children.isEmpty())
        {
            throw new MethodExpressionException(repo.getRepositoryClass(), method);
        }
        return this;
    }

    @Override
    protected QueryPart buildQuery(QueryBuilderContext ctx)
    {
        ctx.append(QueryBuilder.selectQuery(entityName));
        if (hasChildren(excludedForWhereCheck()))
        {
            ctx.append(" where ");
        }
        buildQueryForChildren(ctx);
        return this;
    }

    protected String createJpql()
    {
        QueryBuilderContext ctx = new QueryBuilderContext();
        buildQuery(ctx);
        jpqlQuery = ctx.resultString();
        log.log(Level.FINER, "createJpql: Query is {0}", jpqlQuery);
        return jpqlQuery;
    }

    private Set<Class<? extends QueryPart>> excludedForWhereCheck()
    {
        Set<Class<? extends QueryPart>> excluded = new HashSet<Class<? extends QueryPart>>();
        excluded.add(OrderByQueryPart.class);
        return excluded;
    }

    private boolean hasQueryConditions(String[] orderByParts)
    {
        return !queryPrefix.equals(orderByParts[0]);
    }

    private String removePrefix(String queryPart)
    {
        if (queryPart.startsWith(queryPrefix))
        {
            return queryPart.substring(queryPrefix.length());
        }
        return queryPart;
    }

}
