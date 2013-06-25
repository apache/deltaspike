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
package org.apache.deltaspike.data.impl.meta;

import static org.apache.deltaspike.data.impl.util.QueryUtils.isNotEmpty;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.impl.builder.MethodExpressionException;
import org.apache.deltaspike.data.impl.builder.part.QueryRoot;
import org.apache.deltaspike.data.impl.builder.result.QueryProcessor;
import org.apache.deltaspike.data.impl.builder.result.QueryProcessorFactory;

/**
 * Stores information about a specific method of a Repository:
 * <ul>
 * <li>The reference to the Method reflection object</li>
 * <li>Whether this method delegates, is annotated or is parsed</li>
 * <li>A reference to the parent Repository</li>
 * <li>For parsed Repository methods, also the JPQL string is cached</li>
 * </ul>
 *
 * @author thomashug
 */
public class RepositoryMethod
{

    private final Method method;
    private final MethodType methodType;
    private final RepositoryComponent repo;
    private final QueryRoot queryRoot;
    private final QueryProcessor queryProcessor;

    public RepositoryMethod(Method method, RepositoryComponent repo)
    {
        this.method = method;
        this.repo = repo;
        this.methodType = extractMethodType();
        this.queryRoot = initQueryRoot();
        this.queryProcessor = QueryProcessorFactory.newInstance(method).build();
    }

    public boolean returns(Class<?> returnType)
    {
        return returnType.equals(method.getReturnType());
    }

    private MethodType extractMethodType()
    {
        if (isAnnotated())
        {
            return MethodType.ANNOTATED;
        }
        if (isMethodExpression())
        {
            return MethodType.PARSE;
        }
        return MethodType.DELEGATE;
    }

    private QueryRoot initQueryRoot()
    {
        if (methodType == MethodType.PARSE)
        {
            return QueryRoot.create(method.getName(), repo);
        }
        return QueryRoot.UNKNOWN_ROOT;
    }

    private boolean isAnnotated()
    {
        if (method.isAnnotationPresent(Query.class))
        {
            Query query = method.getAnnotation(Query.class);
            return isValid(query);
        }
        return false;
    }

    private boolean isValid(Query query)
    {
        return isNotEmpty(query.value()) || isNotEmpty(query.named());
    }

    private boolean isMethodExpression()
    {
        if (!Modifier.isAbstract(method.getModifiers()))
        {
            return false;
        }
        try
        {
            QueryRoot.create(method.getName(), repo);
            return true;
        }
        catch (MethodExpressionException e)
        {
            return false;
        }
    }

    public MethodType getMethodType()
    {
        return methodType;
    }

    public RepositoryComponent getRepository()
    {
        return repo;
    }

    public QueryRoot getQueryRoot()
    {
        return queryRoot;
    }

    public QueryProcessor getQueryProcessor()
    {
        return queryProcessor;
    }

}
