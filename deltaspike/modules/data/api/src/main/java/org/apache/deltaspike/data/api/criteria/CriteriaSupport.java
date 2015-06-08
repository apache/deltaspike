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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.SingularAttribute;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Interface to be added to a repository for criteria support.
 *
 * @param <E> Entity type.
 */
public interface CriteriaSupport<E>
{

    /**
     * Create a {@link Criteria} instance.
     *
     * @return Criteria instance related to the Repository entity class.
     */
    Criteria<E, E> criteria();

    /**
     * Create a {@link Criteria} instance.
     *
     * @param <T>   Type related to the current criteria class.
     * @param clazz Class other than the current entity class.
     * @return Criteria instance related to a join type of the current entity class.
     */
    <T> Criteria<T, T> where(Class<T> clazz);

    /**
     * Create a {@link Criteria} instance with a join type.
     *
     * @param <T>      Type related to the current criteria class.
     * @param clazz    Class other than the current entity class.
     * @param joinType Join type to apply.
     * @return Criteria instance related to a join type of the current entity class.
     */
    <T> Criteria<T, T> where(Class<T> clazz, JoinType joinType);

    /**
     * Create a query selection for an Entity attribute.
     *
     * @param attribute Attribute to show up in the result selection
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    <X> QuerySelection<E, X> attribute(SingularAttribute<? super E, X> attribute);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#abs(javax.persistence.criteria.Expression)}
     * over an attribute.
     *
     * @param attribute Attribute to use in the aggregate.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    <N extends Number> QuerySelection<E, N> abs(SingularAttribute<? super E, N> attribute);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#avg(javax.persistence.criteria.Expression)}
     * over an attribute.
     *
     * @param attribute Attribute to use in the aggregate.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    <N extends Number> QuerySelection<E, N> avg(SingularAttribute<? super E, N> attribute);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#count(javax.persistence.criteria.Expression)}
     * over an attribute.
     *
     * @param attribute Attribute to use in the aggregate.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    QuerySelection<E, Long> count(SingularAttribute<? super E, ?> attribute);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#countDistinct(javax.persistence.criteria.Expression)}
     * over an attribute.
     *
     * @param attribute Attribute to use in the aggregate.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    QuerySelection<E, Long> countDistinct(SingularAttribute<? super E, ?> attribute);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#max(javax.persistence.criteria.Expression)}
     * over an attribute.
     *
     * @param attribute Attribute to use in the aggregate.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    <N extends Number> QuerySelection<E, N> max(SingularAttribute<? super E, N> attribute);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#min(javax.persistence.criteria.Expression)}
     * over an attribute.
     *
     * @param attribute Attribute to use in the aggregate.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    <N extends Number> QuerySelection<E, N> min(SingularAttribute<? super E, N> attribute);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#neg(javax.persistence.criteria.Expression)}
     * over an attribute.
     *
     * @param attribute Attribute to use in the aggregate.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    <N extends Number> QuerySelection<E, N> neg(SingularAttribute<? super E, N> attribute);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#sum(javax.persistence.criteria.Expression)}
     * over an attribute.
     *
     * @param attribute Attribute to use in the aggregate.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    <N extends Number> QuerySelection<E, N> sum(SingularAttribute<? super E, N> attribute);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#mod(javax.persistence.criteria.Expression, Integer)}
     * for an attribute.
     *
     * @param attribute Attribute to use in the aggregate.
     * @param modulo    Modulo what.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    QuerySelection<E, Integer> modulo(SingularAttribute<? super E, Integer> attribute, Integer modulo);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#upper(javax.persistence.criteria.Expression)}
     * over a String attribute.
     *
     * @param attribute Attribute to uppercase.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    QuerySelection<E, String> upper(SingularAttribute<? super E, String> attribute);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#lower(javax.persistence.criteria.Expression)}
     * over a String attribute.
     *
     * @param attribute Attribute to lowercase.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    QuerySelection<E, String> lower(SingularAttribute<? super E, String> attribute);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#substring(javax.persistence.criteria.Expression, int)}
     * over a String attribute.
     *
     * @param attribute Attribute to create a substring from.
     * @param from      Substring start.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    QuerySelection<E, String> substring(SingularAttribute<? super E, String> attribute, int from);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#substring(javax.persistence.criteria.Expression, int, int)}
     * over a String attribute.
     *
     * @param attribute Attribute to create a substring from.
     * @param from      Substring start.
     * @param length    Substring length.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    QuerySelection<E, String> substring(SingularAttribute<? super E, String> attribute, int from, int length);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#trim(javax.persistence.criteria.Expression)}
     * over a String attribute.
     *
     * @param attribute Attribute to apply trim.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    QuerySelection<E, String> trim(SingularAttribute<? super E, String> attribute);

    /**
     * Create a query selection for the
     * {@link javax.persistence.criteria.CriteriaBuilder#trim(javax.persistence.criteria.CriteriaBuilder.Trimspec,
     * javax.persistence.criteria.Expression)}
     * over a String attribute.
     *
     * @param trimspec  Used to specify how strings are trimmed.
     * @param attribute Attribute to apply trim.
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    QuerySelection<E, String> trim(CriteriaBuilder.Trimspec trimspec, SingularAttribute<? super E, String> attribute);


    /**
     * Create a query selection for the {@link javax.persistence.criteria.CriteriaBuilder#currentDate()}.
     *
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    QuerySelection<E, Date> currDate();

    /**
     * Create a query selection for the {@link javax.persistence.criteria.CriteriaBuilder#currentTime()}.
     *
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    QuerySelection<E, Time> currTime();

    /**
     * Create a query selection for the {@link javax.persistence.criteria.CriteriaBuilder#currentTimestamp()}.
     *
     * @return {@link QuerySelection} part of a {@link Criteria#select(Class, QuerySelection...)} call.
     */
    QuerySelection<E, Timestamp> currTStamp();

}
