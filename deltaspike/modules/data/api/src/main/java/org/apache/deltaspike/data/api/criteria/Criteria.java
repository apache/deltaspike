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
package org.apache.deltaspike.data.api.criteria;

import java.util.Collection;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Criteria API utilities.
 *
 * @param <C> Entity type.
 * @param <R> Result type.
 */
public interface Criteria<C, R>
{

    /**
     * Executes the query and returns the result list.
     * @return List of entities matching the query.
     */
    List<R> getResultList();

    /**
     * Executes the query which has a single result.
     * @return Entity matching the search query.
     */
    R getSingleResult();

    /**
     * Executes the query which has a single result. Returns {@code null}
     * if there is no result.
     * @return Entity matching the search query, or {@code null} if there is none.
     */
    R getOptionalResult();

    /**
     * Executes the query and returns a single result. If there are
     * multiple results, the first received is returned.
     * @return Entity matching the search query.
     */
    R getAnyResult();

    /**
     * Creates a JPA query object to be executed.
     * @return A {@link TypedQuery} object ready to return results.
     */
    TypedQuery<R> createQuery();

    /**
     * Boolean OR with another Criteria.
     * @param criteria      The right side of the boolean OR.
     * @return              Fluent API: Criteria instance.
     */
    Criteria<C, R> or(Criteria<C, R>... criteria);

    /**
     * Boolean OR with another Criteria.
     * @param criteria      The right side of the boolean OR.
     * @return              Fluent API: Criteria instance.
     */
    Criteria<C, R> or(Collection<Criteria<C, R>> criteria);

    /**
     * Join an attribute with another Criteria.
     * @param att           The attribute to join.
     * @param criteria      The join criteria.
     * @return              Fluent API: Criteria instance.
     */
    <P, E> Criteria<C, R> join(SingularAttribute<? super C, P> att, Criteria<P, P> criteria);

    /**
     * Join a collection attribute with another Criteria.
     * @param att           The attribute to join.
     * @param criteria      The join criteria.
     * @return              Fluent API: Criteria instance.
     */
    <P, E> Criteria<C, R> join(ListAttribute<? super C, P> att, Criteria<P, P> criteria);

    /**
     * Join a collection attribute with another Criteria.
     * @param att           The attribute to join.
     * @param criteria      The join criteria.
     * @return              Fluent API: Criteria instance.
     */
    <P, E> Criteria<C, R> join(CollectionAttribute<? super C, P> att, Criteria<P, P> criteria);

    /**
     * Join a collection attribute with another Criteria.
     * @param att           The attribute to join.
     * @param criteria      The join criteria.
     * @return              Fluent API: Criteria instance.
     */
    <P, E> Criteria<C, R> join(SetAttribute<? super C, P> att, Criteria<P, P> criteria);

    /**
     * Join a collection attribute with another Criteria.
     * @param att           The attribute to join.
     * @param criteria      The join criteria.
     * @return              Fluent API: Criteria instance.
     */
    <P, E> Criteria<C, R> join(MapAttribute<? super C, E, P> att, Criteria<P, P> criteria);


    /**
     * Fetch join an attribute.
     * @param att           The attribute to fetch.
     * @return              Fluent API: Criteria instance.
     */
    <P, E> Criteria<C, R> fetch(SingularAttribute<? super C, P> att);

    /**
     * Fetch join an attribute.
     * @param att           The attribute to fetch.
     * @param joinType      The JoinType to use.
     * @return              Fluent API: Criteria instance.
     */
    <P, E> Criteria<C, R> fetch(SingularAttribute<? super C, P> att, JoinType joinType);

    /**
     * Fetch join an attribute.
     * @param att           The attribute to fetch.
     * @return              Fluent API: Criteria instance.
     */
    <P, E> Criteria<C, R> fetch(PluralAttribute<? super C, P, E> att);

    /**
     * Fetch join an attribute.
     * @param att           The attribute to fetch.
     * @param joinType      The JoinType to use.
     * @return              Fluent API: Criteria instance.
     */
    <P, E> Criteria<C, R> fetch(PluralAttribute<? super C, P, E> att, JoinType joinType);

    /**
     * Apply sorting by an attribute, ascending direction.
     * @param att           The attribute to order for.
     * @return              Fluent API: Criteria instance.
     */
    <P> Criteria<C, R> orderAsc(SingularAttribute<? super C, P> att);

    /**
     * Apply sorting by an attribute, descending direction.
     * @param att           The attribute to order for.
     * @return              Fluent API: Criteria instance.
     */
    <P> Criteria<C, R> orderDesc(SingularAttribute<? super C, P> att);

    /**
     * Create a select query.
     * @param resultClass   The query result class.
     * @param selection     List of selects (attributes, scalars...)
     * @return              Fluent API: Criteria instance.
     */
    <N> Criteria<C, N> select(Class<N> resultClass, QuerySelection<? super C, ?>... selection);

    /**
     * Create a select query.
     * @param selection     List of selects (attributes, scalars...)
     * @return              Fluent API: Criteria instance.
     */
    Criteria<C, Object[]> select(QuerySelection<? super C, ?>... selection);

    /**
     * Apply a distinct on the query.
     * @return              Fluent API: Criteria instance.
     */
    Criteria<C, R> distinct();

    /**
     * Equals predicate.
     * @param att           The attribute to compare with.
     * @param value         The comparison value.
     * @return              Fluent API: Criteria instance.
     */
    <P> Criteria<C, R> eq(SingularAttribute<? super C, P> att, P value);

    /**
     * Equals predicate, case insensitive.
     * @param att           The attribute to compare with.
     * @param value         The comparison value.
     * @return              Fluent API: Criteria instance.
     */
    <P> Criteria<C, R> eqIgnoreCase(SingularAttribute<? super C, String> att, String value);

    /**
     * Not Equals predicate.
     * @param att           The attribute to compare with.
     * @param value         The comparison value.
     * @return              Fluent API: Criteria instance.
     */
    <P> Criteria<C, R> notEq(SingularAttribute<? super C, P> att, P value);

    /**
     * Not Equals predicate, case insensitive.
     * @param att           The attribute to compare with.
     * @param value         The comparison value.
     * @return              Fluent API: Criteria instance.
     */
    <P> Criteria<C, R> notEqIgnoreCase(SingularAttribute<? super C, String> att, String value);

    /**
     * Like predicate.
     * @param att           The attribute to compare with.
     * @param value         The comparison value.
     * @return              Fluent API: Criteria instance.
     */
    <P> Criteria<C, R> like(SingularAttribute<? super C, String> att, String value);

    /**
     * Like predicate, case insensitive.
     * @param att           The attribute to compare with.
     * @param value         The comparison value.
     * @return              Fluent API: Criteria instance.
     */
    <P> Criteria<C, R> likeIgnoreCase(SingularAttribute<? super C, String> att, String value);

    /**
     * Not like predicate.
     * @param att           The attribute to compare with.
     * @param value         The comparison value.
     * @return              Fluent API: Criteria instance.
     */
    <P> Criteria<C, R> notLike(SingularAttribute<? super C, String> att, String value);

    /**
     * Not like predicate, case insensitive.
     * @param att           The attribute to compare with.
     * @param value         The comparison value.
     * @return              Fluent API: Criteria instance.
     */
    <P> Criteria<C, R> notLikeIgnoreCase(SingularAttribute<? super C, String> att, String value);

    /**
     * Less than predicate.
     * @param att           The attribute to compare with.
     * @param value         The comparison value.
     * @return              Fluent API: Criteria instance.
     */
    <P extends Comparable<? super P>> Criteria<C, R> lt(SingularAttribute<? super C, P> att, P value);

    /**
     * Less than or equals predicate.
     * @param att           The attribute to compare with.
     * @param value         The comparison value.
     * @return              Fluent API: Criteria instance.
     */
    <P extends Comparable<? super P>> Criteria<C, R> ltOrEq(SingularAttribute<? super C, P> att, P value);

    /**
     * Greater than predicate.
     * @param att           The attribute to compare with.
     * @param value         The comparison value.
     * @return              Fluent API: Criteria instance.
     */
    <P extends Comparable<? super P>> Criteria<C, R> gt(SingularAttribute<? super C, P> att, P value);

    /**
     * Greater than or equals predicate.
     * @param att           The attribute to compare with.
     * @param value         The comparison value.
     * @return              Fluent API: Criteria instance.
     */
    <P extends Comparable<? super P>> Criteria<C, R> gtOrEq(SingularAttribute<? super C, P> att, P value);

    /**
     * Between predicate.
     * @param att           The attribute to compare with.
     * @param lower         The lower bound comparison value.
     * @param upper         The upper bound comparison value.
     * @return              Fluent API: Criteria instance.
     */
    <P extends Comparable<? super P>> Criteria<C, R> between(SingularAttribute<? super C, P> att, P lower, P upper);

    /**
     * IsNull predicate.
     * @param att           The null attribute.
     * @return              Fluent API: Criteria instance.
     */
    <P> Criteria<C, R> isNull(SingularAttribute<? super C, P> att);

    /**
     * NotNull predicate.
     * @param att           The non-null attribute.
     * @return              Fluent API: Criteria instance.
     */
    <P> Criteria<C, R> notNull(SingularAttribute<? super C, P> att);

    /**
     * Empty predicate.
     * @param att           The collection attribute to check for emptyness.
     * @return              Fluent API: Criteria instance.
     */
    <P extends Collection<?>> Criteria<C, R> empty(SingularAttribute<? super C, P> att);

    /**
     * Not empty predicate.
     * @param att           The collection attribute to check for non-emptyness.
     * @return              Fluent API: Criteria instance.
     */
    <P extends Collection<?>> Criteria<C, R> notEmpty(SingularAttribute<? super C, P> att);

    /**
     * In predicte.
     * @param att           The attribute to check for.
     * @param values        The values for the in predicate.
     * @return
     */
    <P> Criteria<C, R> in(SingularAttribute<? super C, P> att, P... values);

    /**
     * Return the list of predicates applicable for this Criteria instance.
     * @param builder       A CriteriaBuilder used to instantiate the Predicates.
     * @param path          Current path.
     * @return              List of predicates applicable to this Criteria.
     */
    List<Predicate> predicates(CriteriaBuilder builder, Path<C> path);

}
