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

import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

public class FetchBuilder<P, R, E> implements PredicateBuilder<P>
{

    private final JoinType joinType;

    private SingularAttribute<? super P, R> singular;
    private PluralAttribute<? super P, R, E> plural;

    public FetchBuilder(SingularAttribute<? super P, R> singular, JoinType joinType)
    {
        this.joinType = joinType;
        this.singular = singular;
    }

    public FetchBuilder(PluralAttribute<? super P, R, E> plural, JoinType joinType)
    {
        this.joinType = joinType;
        this.plural = plural;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<Predicate> build(CriteriaBuilder builder, Path<P> path)
    {
        if (singular != null)
        {
            fetchSingular((From) path);
        }
        else if (plural != null)
        {
            fetchPlural((From) path);
        }
        return Collections.emptyList();
    }

    SingularAttribute<? super P, R> getSingular()
    {
        return singular;
    }

    void setSingular(SingularAttribute<? super P, R> singular)
    {
        this.singular = singular;
    }

    PluralAttribute<? super P, R, E> getPlural()
    {
        return plural;
    }

    void setPlural(PluralAttribute<? super P, R, E> plural)
    {
        this.plural = plural;
    }

    JoinType getJoinType()
    {
        return joinType;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void fetchSingular(From path)
    {
        if (joinType == null)
        {
            path.fetch(singular);
        }
        else
        {
            path.fetch(singular, joinType);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void fetchPlural(From path)
    {
        if (joinType == null)
        {
            path.fetch(plural);
        }
        else
        {
            path.fetch(plural, joinType);
        }
    }

}
