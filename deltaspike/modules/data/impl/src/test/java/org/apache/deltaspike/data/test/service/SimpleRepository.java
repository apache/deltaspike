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

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.FirstResult;
import org.apache.deltaspike.data.api.MaxResults;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.test.domain.Simple;

@Repository
public abstract class SimpleRepository extends AbstractEntityRepository<Simple, Long>
{

    public List<Simple> implementedQueryByName(String name)
    {
        String query = "select s from Simple s where s.name = :name";
        return entityManager().createQuery(query, Simple.class)
                .setParameter("name", name)
                .getResultList();
    }

    @Query(named = Simple.BY_NAME_ENABLED, max = 1)
    public abstract List<Simple> findByNamedQueryIndexed(String name, Boolean enabled);

    @Query(named = Simple.BY_NAME_ENABLED)
    public abstract List<Simple> findByNamedQueryRestricted(String name, Boolean enabled,
            @MaxResults int max, @FirstResult Integer first);

    @Query(named = Simple.BY_ID, lock = LockModeType.PESSIMISTIC_WRITE)
    public abstract Simple findByNamedQueryNamed(
            @QueryParam("id") Long id, @QueryParam("enabled") Boolean enabled);

    @Query("select s from Simple s where s.name = ?1")
    public abstract Simple findByQuery(String name);

    @Query("select count(s) from Simple s where s.name = ?1")
    public abstract Long findCountByQuery(String name);

    public abstract Simple findByNameAndEnabled(String name, Boolean enabled);

    public abstract List<Simple> findByOrderByCounterAscIdDesc();

    @Query(value = "SELECT * from SIMPLE_TABLE s WHERE s.name = ?1", isNative = true)
    public abstract List<Simple> findWithNative(String name);

    @Modifying
    @Query("update Simple as s set s.name = ?1 where s.id = ?2")
    public abstract int updateNameForId(String name, Long id);

    @Query(named = Simple.BY_NAME_LIKE)
    public abstract QueryResult<Simple> queryResultWithNamed(String name);

    public abstract QueryResult<Simple> findByName(String name);

    @Override
    protected abstract EntityManager entityManager();

}
