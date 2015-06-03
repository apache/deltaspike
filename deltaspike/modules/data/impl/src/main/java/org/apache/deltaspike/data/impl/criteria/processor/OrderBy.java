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
package org.apache.deltaspike.data.impl.criteria.processor;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.deltaspike.data.impl.builder.OrderDirection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderBy<P, V> implements QueryProcessor<P>
{

    private final Set<OrderByDefinition> orderByDefinitions = new HashSet<OrderByDefinition>();

    public void add(SingularAttribute<? super P, V> att, OrderDirection dir)
    {
        orderByDefinitions.add(new OrderByDefinition(att, dir));
    }

    @Override
    public <R> void process(CriteriaQuery<R> query, CriteriaBuilder builder, Path<P> path)
    {
        List<Order> orders = new ArrayList<Order>();
        for (OrderByDefinition orderByDefinition : orderByDefinitions)
        {
            switch (orderByDefinition.getDir())
            {
                case ASC:
                    orders.add(builder.asc(path.get(orderByDefinition.getAtt())));
                    break;
                default:
                    orders.add(builder.desc(path.get(orderByDefinition.getAtt())));
            }
        }
        query.orderBy(orders);
    }

    private class OrderByDefinition
    {
        private final SingularAttribute<? super P, V> att;
        private final OrderDirection dir;

        public OrderByDefinition(SingularAttribute<? super P, V> att, OrderDirection dir)
        {
            this.att = att;
            this.dir = dir;
        }

        public SingularAttribute<? super P, V> getAtt()
        {
            return att;
        }

        public OrderDirection getDir()
        {
            return dir;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            OrderByDefinition that = (OrderByDefinition) o;

            if (att != null ? !att.equals(that.att) : that.att != null)
            {
                return false;
            }
            return dir == that.dir;

        }

        @Override
        public int hashCode()
        {
            int result = att != null ? att.hashCode() : 0;
            result = 31 * result + (dir != null ? dir.hashCode() : 0);
            return result;
        }
    }
}
