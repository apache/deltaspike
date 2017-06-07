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

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.Query;

import org.apache.deltaspike.data.impl.builder.part.QueryRoot;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.param.Parameters;

@ApplicationScoped
public class MethodQueryBuilder extends QueryBuilder
{

    @Override
    public Object execute(CdiQueryInvocationContext context)
    {
        Query jpaQuery = createJpaQuery(context);
        return context.executeQuery(jpaQuery);
    }

    private Query createJpaQuery(CdiQueryInvocationContext context)
    {
        Parameters params = context.getParams();
        QueryRoot root = context.getRepositoryMethodMetadata().getQueryRoot();
        String jpqlQuery = context.applyQueryStringPostProcessors(root.getJpqlQuery());
        context.setQueryString(jpqlQuery);
        params.updateValues(root.getParameterUpdates());
        Query result = params.applyTo(context.getEntityManager().createQuery(jpqlQuery));
        return context.applyRestrictions(result);
    }

}
