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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.deltaspike.data.impl.builder.QueryBuilderContext;
import org.apache.deltaspike.data.impl.meta.RepositoryMetadata;

public abstract class QueryPart
{

    protected List<QueryPart> children = new LinkedList<QueryPart>();

    protected abstract QueryPart build(String queryPart, String method, RepositoryMetadata repo);

    protected abstract QueryPart buildQuery(QueryBuilderContext ctx);

    protected void buildQueryForChildren(QueryBuilderContext ctx)
    {
        for (QueryPart child : children)
        {
            child.buildQuery(ctx);
        }
    }

    protected boolean hasChildren(Set<Class<? extends QueryPart>> excluded)
    {
        if (children == null || children.isEmpty())
        {
            return false;
        }
        for (QueryPart part : children)
        {
            if (!excluded.contains(part.getClass()))
            {
                return true;
            }
        }
        return false;
    }

}
