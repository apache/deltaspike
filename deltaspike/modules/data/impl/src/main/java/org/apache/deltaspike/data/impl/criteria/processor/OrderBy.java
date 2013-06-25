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
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.deltaspike.data.impl.builder.OrderDirection;

public class OrderBy<P, V> implements QueryProcessor<P>
{

    private final SingularAttribute<? super P, V> att;
    private final OrderDirection dir;

    public OrderBy(SingularAttribute<? super P, V> att, OrderDirection dir)
    {
        this.att = att;
        this.dir = dir;
    }

    @Override
    public <R> void process(CriteriaQuery<R> query, CriteriaBuilder builder, Path<P> path)
    {
        switch (dir)
        {
            case ASC:
                query.orderBy(builder.asc(path.get(att)));
                break;
            default:
                query.orderBy(builder.desc(path.get(att)));
        }
    }

}
