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

import static org.apache.deltaspike.data.impl.meta.MethodType.ANNOTATED;
import static org.apache.deltaspike.data.impl.meta.MethodType.DELEGATE;
import static org.apache.deltaspike.data.impl.meta.MethodType.PARSE;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.api.provider.DependentProvider;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.meta.MethodType;
import org.apache.deltaspike.data.impl.meta.QueryInvocationLiteral;
import org.apache.deltaspike.data.impl.meta.RepositoryMethod;
import org.apache.deltaspike.data.impl.util.bean.DependentProviderDestroyable;

@ApplicationScoped
public class QueryBuilderFactory
{

    private static final Map<MethodType, QueryInvocationLiteral> LITERALS =
            new HashMap<MethodType, QueryInvocationLiteral>()
            {
                private static final long serialVersionUID = 1L;

                {
                    put(ANNOTATED, new QueryInvocationLiteral(ANNOTATED));
                    put(DELEGATE, new QueryInvocationLiteral(DELEGATE));
                    put(PARSE, new QueryInvocationLiteral(PARSE));
                }
            };

    public QueryBuilder build(RepositoryMethod method, CdiQueryInvocationContext context)
    {
        DependentProvider<QueryBuilder> builder = BeanProvider.getDependent(
                QueryBuilder.class, LITERALS.get(method.getMethodType()));
        context.addDestroyable(new DependentProviderDestroyable(builder));
        if (method.returns(QueryResult.class))
        {
            return new WrappedQueryBuilder(builder.get());
        }
        return builder.get();
    }

}
