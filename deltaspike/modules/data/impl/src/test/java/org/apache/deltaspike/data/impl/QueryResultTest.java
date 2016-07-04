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
package org.apache.deltaspike.data.impl;

import static org.apache.deltaspike.data.test.util.TestDeployments.initDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NonUniqueResultException;

import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.*;
import org.apache.deltaspike.data.test.service.SimpleRepository;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(WebProfileCategory.class)
public class QueryResultTest extends TransactionalTestCase
{

    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment()
                .addClasses(SimpleRepository.class)
                .addPackage(Simple.class.getPackage());
    }

    @Inject
    private SimpleRepository repo;

    private SimpleBuilder builder;

    @Test
    public void should_sort_result()
    {
        // given
        final String name = "testSortResult";
        builder.createSimple(name, Integer.valueOf(99));
        builder.createSimple(name, Integer.valueOf(22));
        builder.createSimple(name, Integer.valueOf(22));
        builder.createSimple(name, Integer.valueOf(22));
        builder.createSimple(name, Integer.valueOf(56));
        builder.createSimple(name, Integer.valueOf(123));

        // when
        List<Simple> result = repo.findByName(name)
                .orderDesc(Simple_.counter)
                .orderAsc(Simple_.id)
                .getResultList();

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        int lastCounter = Integer.MAX_VALUE;
        long lastId = Long.MIN_VALUE;
        for (Simple simple : result)
        {
            int currentCounter = simple.getCounter().intValue();
            long currentId = simple.getId().longValue();
            if (currentCounter == lastCounter)
            {
                assertTrue(currentId > lastId);
            }
            else
            {
                assertTrue(currentCounter < lastCounter);
            }
            lastCounter = currentCounter;
            lastId = currentId;
        }
    }

    @Test
    public void should_change_sort_order()
    {
        // given
        final String name = "testChangeSortOrder";
        builder.createSimple(name, Integer.valueOf(99));
        builder.createSimple(name, Integer.valueOf(22));
        builder.createSimple(name, Integer.valueOf(229));

        // when
        QueryResult<Simple> query = repo.findByName(name);
        List<Simple> result1 = query
                .changeOrder(Simple_.counter)
                .getResultList();
        List<Simple> result2 = query
                .changeOrder(Simple_.counter)
                .getResultList();

        // then
        assertEquals(22, result1.get(0).getCounter().intValue());
        assertEquals(229, result2.get(0).getCounter().intValue());
    }

    @Test
    public void should_clear_sort_order()
    {
        // given
        final String name = "testClearSortOrder";
        builder.createSimple(name, Integer.valueOf(99));
        builder.createSimple(name, Integer.valueOf(22));
        builder.createSimple(name, Integer.valueOf(229));

        // when
        QueryResult<Simple> query = repo.findByName(name);
        List<Simple> result1 = query
                .changeOrder(Simple_.counter)
                .getResultList();
        List<Simple> result2 = query
                .clearOrder()
                .getResultList();

        // then
        assertEquals(result1.size(), result2.size());
        for (int i = 0; i < result1.size(); i++)
        {
            int count1 = result1.get(i).getCounter().intValue();
            int count2 = result2.get(i).getCounter().intValue();
            if (count1 != count2)
            {
                return;
            }
        }
        fail("Both collections sorted: " + result1 + "," + result2);
    }

    @Test
    public void should_page_result()
    {
        // given
        final String name = "testPageResult";
        builder.createSimple(name, Integer.valueOf(99));
        builder.createSimple(name, Integer.valueOf(22));
        builder.createSimple(name, Integer.valueOf(22));
        builder.createSimple(name, Integer.valueOf(22));
        builder.createSimple(name, Integer.valueOf(56));
        builder.createSimple(name, Integer.valueOf(123));

        // when
        List<Simple> result = repo.findByName(name)
                .hint("javax.persistence.query.timeout", 10000)
                .lockMode(LockModeType.NONE)
                .flushMode(FlushModeType.COMMIT)
                .orderDesc(Simple_.counter)
                .firstResult(2)
                .maxResults(2)
                .getResultList();

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    public void should_page_with_page_api()
    {
        // given
        final String name = "testPageAPI";
        builder.createSimple(name, Integer.valueOf(22));
        builder.createSimple(name, Integer.valueOf(56));
        builder.createSimple(name, Integer.valueOf(99));
        builder.createSimple(name, Integer.valueOf(123));
        builder.createSimple(name, Integer.valueOf(229));
        builder.createSimple(name, Integer.valueOf(299));
        builder.createSimple(name, Integer.valueOf(389));

        // when
        QueryResult<Simple> pagedQuery = repo
                .findByName(name)
                .withPageSize(2);
        List<Simple> result1 = pagedQuery.getResultList();
        List<Simple> result2 = pagedQuery.nextPage().nextPage().getResultList();
        int current = pagedQuery.currentPage();
        List<Simple> result3 = pagedQuery.toPage(1).getResultList();
        int total = pagedQuery.countPages();
        int pageSize = pagedQuery.pageSize();

        // then
        assertEquals(2, result1.size());
        assertEquals(2, result2.size());
        assertEquals(2, result3.size());
        assertEquals(2, current);
        assertEquals(4, total);
        assertEquals(2, pageSize);

        assertEquals(22, result1.get(0).getCounter().intValue());
        assertEquals(229, result2.get(0).getCounter().intValue());
        assertEquals(99, result3.get(0).getCounter().intValue());

    }

    @Test
    public void should_modify_named_query()
    {
        // given
        final String name = "testModifyNamedQuery";
        builder.createSimple(name + 0);
        builder.createSimple(name + 1);
        builder.createSimple(name + 2);
        builder.createSimple(name + 3);

        // when
        List<Simple> result = repo.queryResultWithNamed(name + "%")
                .orderDesc(Simple_.name)
                .getResultList();

        // then
        assertEquals(4, result.size());
        assertEquals(name + 3, result.get(0).getName());
        assertEquals(name + 2, result.get(1).getName());
    }

    @Test
    public void should_count_with_method_query()
    {
        // given
        final String name = "testCountWithMethodQuery";
        builder.createSimple(name);
        builder.createSimple(name);

        // when
        long result = repo.findByName(name).count();

        // then
        assertEquals(2L, result);
    }

    @Test
    public void should_count_with_named_query()
    {
        // given
        final String name = "testCountWithNamedQuery";
        builder.createSimple(name);
        builder.createSimple(name);

        // when
        long result = repo.queryResultWithNamed(name).count();

        // then
        assertEquals(2L, result);
    }

    @Test
    public void should_count_without_whereclause()
    {
        // given
        final String name = "testCountWithoutWhereclause";
        builder.createSimple(name);
        builder.createSimple(name);

        // when
        long result = repo.queryAll().count();

        // then
        assertEquals(2L, result);
    }

    @Test
    public void should_count_with_orderby()
    {
        // given
        final String name = "testCountWithOrderBy";
        builder.createSimple(name);
        builder.createSimple(name);

        // when
        long result = repo.findByQueryWithOrderBy(name).count();

        // then
        assertEquals(2L, result);
    }

    @Test
    public void should_query_optional()
    {
        // given
        final String name = "should_query_optional";
        builder.createSimple(name);

        // when
        Simple result1 = repo.queryResultWithNamed(name).getOptionalResult();
        Simple result2 = repo.queryResultWithNamed("this_does_not_exist").getOptionalResult();

        // then
        assertNotNull(result1);
        assertEquals(name, result1.getName());
        assertNull(result2);
    }

    @Test(expected = NonUniqueResultException.class)
    public void should_fail_query_optional_with_nonunique()
    {
        // given
        final String name = "should_fail_query_optional_with_nonunique";
        builder.createSimple(name);
        builder.createSimple(name);

        // when
        repo.queryResultWithNamed(name).getOptionalResult();
    }

    @Test
    public void should_query_any()
    {
        // given
        final String name = "should_query_any";
        builder.createSimple(name);
        builder.createSimple(name);

        // when
        Simple result1 = repo.queryResultWithNamed(name).getAnyResult();
        Simple result2 = repo.queryResultWithNamed("this_does_not_exist").getAnyResult();

        // then
        assertNotNull(result1);
        assertEquals(name, result1.getName());
        assertNull(result2);
    }

    @Test
    public void should_paginate_with_orderby()
    {
        // given
        SimpleStringIdBuilder builder = new SimpleStringIdBuilder(getEntityManager());


        final String name = "should_paginate_with_orderby";
        final String name2 = "should_paginate_with_orderby2";
        builder.createSimple("a", name);
        builder.createSimple("b", name2);

        // when
        QueryResult<SimpleStringId> allOrderByNamePaginate = repo.findAllOrderByIdPaginate(0, 10);

        // then
        assertNotNull(allOrderByNamePaginate);

        List<SimpleStringId> resultList = allOrderByNamePaginate.getResultList();
        assertEquals(2, resultList.size());
        assertEquals("a", resultList.get(0).getId());
        assertEquals("b", resultList.get(1).getId());
    }

    @Test
    public void should_sort_all_result()
    {
        List<Simple> result = repo.queryAll()
                .orderDesc("s.counter",false)
                .orderAsc("s.enabled", false)
                .getResultList();
        // no real check here, verifying query syntax passes.
        assertNotNull(result);
    }

    @Test
    public void should_sort_name_results()
    {
        List<Simple> result = repo.queryResultWithNamed("name")
                .orderDesc(Simple_.counter, true)
                .orderAsc(Simple_.id)
                .getResultList();
        // no real check here, verifying query syntax passes.
        assertNotNull(result);
    }

    @Before
    public void setup()
    {
        builder = new SimpleBuilder(getEntityManager());
    }

}
