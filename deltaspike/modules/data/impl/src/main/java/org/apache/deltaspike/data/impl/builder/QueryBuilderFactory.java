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

import static org.apache.deltaspike.data.impl.meta.RepositoryMethodType.ANNOTATED;
import static org.apache.deltaspike.data.impl.meta.RepositoryMethodType.DELEGATE;
import static org.apache.deltaspike.data.impl.meta.RepositoryMethodType.PARSE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodType;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadata;

@ApplicationScoped
public class QueryBuilderFactory
{
    @Inject
    private MethodQueryBuilder methodQueryBuilder;
    @Inject
    private DelegateQueryBuilder delegateQueryBuilder;
    @Inject
    private AnnotatedQueryBuilder annotatedQueryBuilder;
            
    protected QueryBuilder getQueryBuilder(RepositoryMethodType repositoryMethodType)
    {
        switch (repositoryMethodType)
        {
            case ANNOTATED:
                return annotatedQueryBuilder;
            case PARSE:
                return methodQueryBuilder;
            case DELEGATE:
                return delegateQueryBuilder;
            default:
                throw new RuntimeException(
                        "No " + QueryBuilder.class.getName() + " avialable for type: " + repositoryMethodType);
        }
    }

    public QueryBuilder build(RepositoryMethodMetadata methodMetadata, CdiQueryInvocationContext context)
    {
        QueryBuilder builder = getQueryBuilder(context.getRepositoryMethodMetadata().getMethodType());

        if (QueryResult.class.equals(methodMetadata.getMethod().getReturnType()))
        {
            return new WrappedQueryBuilder(builder);
        }

        return builder;
    }

}
