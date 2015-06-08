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
package org.apache.deltaspike.data.test.service;

import java.util.Date;
import java.util.List;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.domain.Simple_;
import org.apache.deltaspike.data.test.domain.SuperSimple_;

import javax.persistence.criteria.CriteriaBuilder;

@Repository
@SuppressWarnings("unchecked")
public abstract class SimpleCriteriaRepository extends AbstractEntityRepository<Simple, Long>
        implements CriteriaSupport<Simple>
{

    public List<Simple> queryByCriteria(String name, Boolean enabled, Integer from, Integer to)
    {
        return criteria()
                .eq(Simple_.name, name)
                .eq(Simple_.enabled, enabled)
                .between(Simple_.counter, from, to)
                .getResultList();
    }

    public List<Simple> queryByIgnoreCase(String name, String nameLike)
    {
        return criteria()
                .or(
                        criteria()
                                .eqIgnoreCase(Simple_.name, name)
                                .notEqIgnoreCase(Simple_.name, nameLike),
                        criteria()
                                .likeIgnoreCase(Simple_.name, nameLike)
                                .notLikeIgnoreCase(Simple_.name, name)
                )
                .getResultList();
    }

    public Simple queryOptional(String name)
    {
        return criteria()
                .eq(Simple_.name, name)
                .getOptionalResult();
    }

    public Simple queryAny(String name)
    {
        return criteria()
                .eq(Simple_.name, name)
                .getAnyResult();
    }

    public List<Simple> findByTimeBetween(Date from, Date to)
    {
        return criteria()
                .gt(Simple_.temporal, from)
                .lt(Simple_.temporal, to)
                .getResultList();
    }

    public Simple findBySuperName(String superName)
    {
        return criteria()
                .eq(SuperSimple_.superName, superName)
                .getSingleResult();
    }

    public Long criteriaCount(String name)
    {
        return criteria()
                .select(Long.class, countDistinct(Simple_.name))
                .eq(Simple_.name, name)
                .getSingleResult();
    }

    public Statistics queryWithSelect(String name)
    {
        return criteria()
                .select(Statistics.class, avg(Simple_.counter), count(Simple_.counter))
                .eq(Simple_.name, name)
                .getSingleResult();
    }

    public Object[] queryWithSelectAggregateReturnArray(String name)
    {
        return criteria()
                .select(min(Simple_.counter), max(Simple_.counter),
                        currDate(), currTime(), currTStamp())
                .eq(Simple_.name, name)
                .createQuery()
                .getSingleResult();
    }

    public List<Object[]> queryWithSelectAttributes(String name)
    {
        return criteria()
                .select(attribute(Simple_.name),
                        upper(Simple_.name), lower(Simple_.name),
                        substring(Simple_.name, 2), substring(Simple_.name, 2, 2))
                .eq(Simple_.name, name)
                .createQuery()
                .getResultList();
    }

    public List<Simple> findOrderByNameAndCounter()
    {
        return criteria()
                .orderDesc(Simple_.counter)
                .orderAsc(Simple_.name)
                .getResultList();
    }

    public Object[] queryWithSelectAttributesAndTrim(String name)
    {
        return criteria()
                .select(attribute(Simple_.name), trim(Simple_.name),
                        trim(CriteriaBuilder.Trimspec.LEADING, Simple_.name))
                .eq(Simple_.name, name)
                .createQuery()
                .getSingleResult();
    }

}
