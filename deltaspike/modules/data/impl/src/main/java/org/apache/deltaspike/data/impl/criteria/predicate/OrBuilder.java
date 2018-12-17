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
package org.apache.deltaspike.data.impl.criteria.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.apache.deltaspike.data.api.criteria.Criteria;

public class OrBuilder<P> implements PredicateBuilder<P>
{

    private final Criteria<P, P>[] criteria;

    public OrBuilder(Criteria<P, P>... criteria)
    {
        this.criteria = criteria;
    }

    @Override
    public List<Predicate> build(CriteriaBuilder builder, Path<P> path)
    {
        List<Predicate> and = new ArrayList<Predicate>(criteria.length);
        for (Criteria<P, P> c : criteria)
        {
            and.add(builder.or(c.predicates(builder, path).toArray(new Predicate[0])));
        }
        return Arrays.asList(builder.or(and.toArray(new Predicate[0])));
    }

}
