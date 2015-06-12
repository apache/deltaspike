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

import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.meta.MethodType;
import org.apache.deltaspike.data.impl.meta.QueryInvocation;
import org.apache.deltaspike.data.impl.param.Parameters;
import org.apache.deltaspike.data.impl.util.jpa.QueryStringExtractorFactory;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;

import static org.apache.deltaspike.data.impl.util.QueryUtils.isNotEmpty;

/**
 * Create the query based on method annotations.
 */
@QueryInvocation(MethodType.ANNOTATED)
public class AnnotatedQueryBuilder extends QueryBuilder
{

    private final QueryStringExtractorFactory factory = new QueryStringExtractorFactory();

    @Override
    public Object execute(CdiQueryInvocationContext context)
    {
        Method method = context.getMethod();
        Query query = method.getAnnotation(Query.class);
        javax.persistence.Query jpaQuery = createJpaQuery(query, context);
        return context.executeQuery(jpaQuery);
    }

    private javax.persistence.Query createJpaQuery(Query query, CdiQueryInvocationContext context)
    {
        EntityManager entityManager = context.getEntityManager();
        Parameters params = context.getParams();
        javax.persistence.Query result = null;
        if (isNotEmpty(query.named()))
        {
            if (!context.hasQueryStringPostProcessors())
            {
                result = params.applyTo(entityManager.createNamedQuery(query.named()));
            }
            else
            {
                javax.persistence.Query namedQuery = entityManager.createNamedQuery(query.named());
                String named = factory.extract(namedQuery);
                String jpqlQuery = context.applyQueryStringPostProcessors(named);
                result = params.applyTo(entityManager.createQuery(jpqlQuery));
            }
        }
        else if (query.isNative())
        {
            String jpqlQuery = context.applyQueryStringPostProcessors(query.value());
            result = params.applyTo(entityManager.createNativeQuery(jpqlQuery));
        }
        else
        {
            String jpqlQuery = context.applyQueryStringPostProcessors(query.value());
            context.setQueryString(jpqlQuery);
            result = params.applyTo(entityManager.createQuery(jpqlQuery));
        }
        return context.applyRestrictions(result);
    }

}
