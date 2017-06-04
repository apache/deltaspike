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

import static org.apache.deltaspike.data.impl.util.QueryUtils.uncapitalize;

import java.text.MessageFormat;

import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.builder.QueryBuilderContext;
import org.apache.deltaspike.data.impl.builder.QueryOperator;
import org.apache.deltaspike.data.impl.meta.RepositoryMetadata;
import org.apache.deltaspike.data.impl.param.ToUpperStringParameterUpdate;

class PropertyQueryPart extends BasePropertyQueryPart
{

    private String name;
    private QueryOperator comparator;

    @Override
    protected QueryPart build(String queryPart, String method, RepositoryMetadata repo)
    {
        comparator = QueryOperator.Equal;
        name = uncapitalize(queryPart);
        for (QueryOperator comp : QueryOperator.values())
        {
            if (queryPart.endsWith(comp.getExpression()))
            {
                comparator = comp;
                name = uncapitalize(queryPart.substring(0, queryPart.indexOf(comp.getExpression())));
                break;
            }
        }
        validate(name, method, repo);
        name = rewriteSeparator(name);
        return this;
    }

    @Override
    protected QueryPart buildQuery(QueryBuilderContext ctx)
    {
        String[] args = new String[comparator.getParamNum() + 1];
        args[0] = QueryBuilder.ENTITY_NAME + "." + name;
        for (int i = 1; i < args.length; i++)
        {
            args[i] = "?" + ctx.increment();
        }
        ctx.append(MessageFormat.format(comparator.getJpql(), (Object[]) args));
        if (comparator.isCaseInsensitive() && args.length >= 1)
        {
            ctx.addParameterUpdate(new ToUpperStringParameterUpdate(args[1].substring(1)));
        }
        return this;
    }

}
