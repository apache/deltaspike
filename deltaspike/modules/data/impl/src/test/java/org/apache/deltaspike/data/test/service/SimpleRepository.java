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

import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;
import static org.apache.deltaspike.data.api.SingleResultType.ANY;
import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.FirstResult;
import org.apache.deltaspike.data.api.MaxResults;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.domain.SimpleStringId;

@Repository
public abstract class SimpleRepository extends AbstractEntityRepository<Simple, Long>
{

    public List<Simple> implementedQueryByName(String name)
    {
        String query = "select s from Simple s where s.name = :name";
        return typedQuery(query)
                .setParameter("name", name)
                .getResultList();
    }

    @Query(named = Simple.BY_NAME_ENABLED, max = 1)
    public abstract List<Simple> findByNamedQueryIndexed(String name, Boolean enabled);

    @Query(named = Simple.BY_NAME_LIKE, singleResult = OPTIONAL)
    public abstract Simple findByNameOptional(String name);

    @Query(named = Simple.BY_NAME_LIKE, singleResult = ANY)
    public abstract Simple findByNameAny(String name);

    @Query(named = Simple.BY_NAME_ENABLED)
    public abstract List<Simple> findByNamedQueryRestricted(String name, Boolean enabled,
            @MaxResults int max, @FirstResult Integer first);

    @Query(named = Simple.BY_ID, lock = PESSIMISTIC_WRITE)
    public abstract Simple findByNamedQueryNamed(
            @QueryParam("id") Long id, @QueryParam("enabled") Boolean enabled);

    @Query("select s from Simple s where s.name = ?1")
    public abstract Simple findByQuery(String name);

    @Query("select count(s) from Simple s where s.name = ?1")
    public abstract Long findCountByQuery(String name);

    @Query("select s from Simple s where s.name = ?1 order by s.counter desc")
    public abstract QueryResult<Simple> findByQueryWithOrderBy(String name);

    public abstract Simple findByNameAndEnabled(String name, Boolean enabled);

    public abstract Simple findByNameLikeIgnoreCase(String name);

    public abstract Simple findByNameIgnoreCase(String name);

    public abstract Simple findOptionalByName(String name);

    public abstract Simple findAnyByName(String name);

    public abstract List<Simple> findByOrderByCounterAscIdDesc();

    @Query(value = "SELECT * from SIMPLE_TABLE s WHERE s.name = ?1", isNative = true)
    public abstract List<Simple> findWithNative(String name);

    @Modifying
    @Query("update Simple as s set s.name = ?1 where s.id = ?2")
    public abstract int updateNameForId(String name, Long id);

    @Query(named = Simple.BY_NAME_LIKE)
    public abstract QueryResult<Simple> queryResultWithNamed(String name);

    @Query("select s from Simple s")
    public abstract QueryResult<Simple> queryAll();

    public abstract QueryResult<Simple> findByName(String name);

    @Query(named = SimpleStringId.FIND_ALL_ORDER_BY_ID)
    public abstract QueryResult<SimpleStringId> findAllOrderByIdPaginate(@FirstResult int start, @MaxResults int pageSize);

    public abstract void deleteByName(String name);

    public abstract void removeByName(String name);

    public abstract void deleteByNameAndEnabled(String name, boolean enable);

    public abstract void removeByNameAndEnabled(String name, Boolean aTrue);

    @Override
    protected abstract EntityManager entityManager();
}
