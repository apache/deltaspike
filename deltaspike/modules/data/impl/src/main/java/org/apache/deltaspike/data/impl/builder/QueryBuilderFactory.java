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

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodType;
import org.apache.deltaspike.data.impl.meta.QueryInvocationLiteral;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadata;

@ApplicationScoped
public class QueryBuilderFactory
{
    private static final Map<RepositoryMethodType, QueryInvocationLiteral> LITERALS =
            new HashMap<RepositoryMethodType, QueryInvocationLiteral>()
            {
                private static final long serialVersionUID = 1L;

                {
                    put(ANNOTATED, new QueryInvocationLiteral(ANNOTATED));
                    put(DELEGATE, new QueryInvocationLiteral(DELEGATE));
                    put(PARSE, new QueryInvocationLiteral(PARSE));
                }
            };

    public QueryBuilder build(RepositoryMethodMetadata methodMetadata, CdiQueryInvocationContext context)
    {
        QueryBuilder builder = BeanProvider.getContextualReference(
                QueryBuilder.class, LITERALS.get(methodMetadata.getMethodType()));

        if (QueryResult.class.equals(methodMetadata.getMethod().getReturnType()))
        {
            return new WrappedQueryBuilder(builder);
        }

        return builder;
    }

}
