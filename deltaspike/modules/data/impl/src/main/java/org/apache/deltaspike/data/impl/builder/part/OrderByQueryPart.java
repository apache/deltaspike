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

import static org.apache.deltaspike.core.util.StringUtils.isNotEmpty;
import static org.apache.deltaspike.data.impl.util.QueryUtils.splitByKeyword;
import static org.apache.deltaspike.data.impl.util.QueryUtils.uncapitalize;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.builder.QueryBuilderContext;
import org.apache.deltaspike.data.impl.meta.RepositoryMetadata;

public class OrderByQueryPart extends BasePropertyQueryPart
{
    private static final String KEYWORD_ASC = "Asc";
    private static final String KEYWORD_DESC = "Desc";

    private final List<OrderByQueryAttribute> attributes = new LinkedList<OrderByQueryAttribute>();

    @Override
    protected QueryPart build(String queryPart, String method, RepositoryMetadata repo)
    {
        Set<String> collect = new LinkedHashSet<String>();
        List<String> ascSplit = new LinkedList<String>();
        split(queryPart, KEYWORD_ASC, ascSplit);
        for (String ascPart : ascSplit)
        {
            split(ascPart, KEYWORD_DESC, collect);
        }
        for (String part : collect)
        {
            Direction direction = Direction.fromQueryPart(part);
            String attribute = direction.attribute(part);
            validate(attribute, method, repo);
            attributes.add(new OrderByQueryAttribute(attribute, direction));
        }
        return this;
    }

    @Override
    protected QueryPart buildQuery(QueryBuilderContext ctx)
    {
        ctx.append(" order by ");
        for (Iterator<OrderByQueryAttribute> it = attributes.iterator(); it.hasNext();)
        {
            it.next().buildQuery(ctx);
            if (it.hasNext())
            {
                ctx.append(", ");
            }
        }
        return this;
    }

    private void split(String queryPart, String keyword, Collection<String> result)
    {
        for (String part : splitByKeyword(queryPart, keyword))
        {
            String attribute = !part.endsWith(KEYWORD_DESC) && !part.endsWith(KEYWORD_ASC) ? part + keyword : part;
            result.add(attribute);
        }
    }

    private class OrderByQueryAttribute
    {

        private final String attribute;
        private final Direction direction;

        public OrderByQueryAttribute(String attribute, Direction direction)
        {
            this.attribute = attribute;
            this.direction = direction;
        }

        protected void buildQuery(QueryBuilderContext ctx)
        {
            String entityPrefix = QueryBuilder.ENTITY_NAME + ".";
            ctx.append(entityPrefix).append(rewriteSeparator(attribute))
                    .append(direction.queryDirection());
        }
    }

    private static enum Direction
    {
        ASC(KEYWORD_ASC),
        DESC(KEYWORD_DESC),
        DEFAULT("");

        private final String postfix;

        private Direction(String postfix)
        {
            this.postfix = postfix;
        }

        public boolean endsWith(String queryPart)
        {
            return isNotEmpty(postfix) ? queryPart.endsWith(postfix) : false;
        }

        public String attribute(String queryPart)
        {
            String attribute = isNotEmpty(postfix) ?
                    queryPart.substring(0, queryPart.indexOf(postfix)) :
                    queryPart;
            return uncapitalize(attribute);
        }

        public String queryDirection()
        {
            return isNotEmpty(postfix) ? " " + postfix.toLowerCase() : "";
        }

        public static Direction fromQueryPart(String queryPart)
        {
            for (Direction dir : values())
            {
                if (dir.endsWith(queryPart))
                {
                    return dir;
                }
            }
            return DEFAULT;
        }

    }

}
