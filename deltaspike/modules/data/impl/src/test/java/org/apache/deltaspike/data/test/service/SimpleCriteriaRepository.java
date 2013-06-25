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

import java.util.List;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.domain.Simple_;

@Repository
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

    @SuppressWarnings("unchecked")
    public Statistics queryWithSelect(String name)
    {
        return criteria()
                .select(Statistics.class, avg(Simple_.counter), count(Simple_.counter))
                .eq(Simple_.name, name)
                .getSingleResult();
    }

    @SuppressWarnings("unchecked")
    public Object[] queryWithSelectAggregateReturnArray(String name)
    {
        return criteria()
                .select(min(Simple_.counter), max(Simple_.counter),
                        currDate(), currTime(), currTStamp())
                .eq(Simple_.name, name)
                .createQuery()
                .getSingleResult();
    }

    @SuppressWarnings("unchecked")
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

}
