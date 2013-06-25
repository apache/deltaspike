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
package org.apache.deltaspike.data.impl.builder.postprocessor;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.deltaspike.data.impl.builder.OrderDirection;
import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.handler.QueryStringPostProcessor;

public class OrderByQueryStringPostProcessor implements QueryStringPostProcessor
{

    private static final String ORDER_BY = " order by ";

    private final String attribute;
    private OrderDirection direction;

    public OrderByQueryStringPostProcessor(SingularAttribute<?, ?> attribute, OrderDirection direction)
    {
        this.attribute = attribute.getName();
        this.direction = direction;
    }

    public OrderByQueryStringPostProcessor(String attribute, OrderDirection direction)
    {
        this.attribute = attribute;
        this.direction = direction;
    }

    @Override
    public String postProcess(String queryString)
    {
        StringBuilder builder = new StringBuilder(queryString);
        if (queryString.contains(ORDER_BY))
        {
            builder.append(",");
        }
        else
        {
            builder.append(ORDER_BY);
        }
        return builder.append(QueryBuilder.ENTITY_NAME)
                .append(".").append(attribute)
                .append(" ").append(direction)
                .toString();
    }

    public boolean matches(SingularAttribute<?, ?> attribute)
    {
        return matches(attribute.getName());
    }

    public boolean matches(String attribute)
    {
        return this.attribute.equals(attribute);
    }

    public void changeDirection()
    {
        direction = direction.change();
    }

}
