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
package org.apache.deltaspike.data.impl.criteria.selection.numeric;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.metamodel.SingularAttribute;

import org.apache.deltaspike.data.api.criteria.QuerySelection;

public class Count<P> implements QuerySelection<P, Long>
{

    private final SingularAttribute<? super P, ?> attribute;

    public Count(SingularAttribute<? super P, ?> attribute)
    {
        this.attribute = attribute;
    }

    @Override
    public <R> Selection<Long> toSelection(CriteriaQuery<R> query, CriteriaBuilder builder, Path<? extends P> path)
    {
        return builder.count(path.get(attribute));
    }
}
