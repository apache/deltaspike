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
package org.apache.deltaspike.data.impl.criteria;

import static org.apache.deltaspike.data.test.util.TestDeployments.initDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;

import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.OneToMany;
import org.apache.deltaspike.data.test.domain.OneToOne;
import org.apache.deltaspike.data.test.domain.Parent;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.service.ParentRepository;
import org.apache.deltaspike.data.test.service.SimpleCriteriaRepository;
import org.apache.deltaspike.data.test.service.Statistics;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(WebProfileCategory.class)
public class CriteriaTest extends TransactionalTestCase
{

    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment()
                .addClasses(SimpleCriteriaRepository.class, ParentRepository.class, Statistics.class)
                .addPackage(Simple.class.getPackage());
    }

    @Inject
    private SimpleCriteriaRepository repo;

    @Inject
    private ParentRepository parentRepo;

    @Test
    public void should_create_criteria_query()
    {
        // given
        final String name = "testCreateCriteriaQuery";
        createSimple(name, 55);

        // when
        List<Simple> result1 = repo.queryByCriteria(name, Boolean.TRUE, 0, 50);
        List<Simple> result2 = repo.queryByCriteria(name, Boolean.TRUE, 50, 100);
        List<Simple> result3 = repo.queryByCriteria(name, Boolean.FALSE, 50, 100);

        // then
        assertEquals(0, result1.size());
        assertEquals(1, result2.size());
        assertEquals(0, result3.size());
    }

    @Test
    public void should_query_with_ignore_case()
    {
        // given
        final String name = "TEST_query_EQ_with_igNOREe_case";
        final String nameLike = "TEST_query_LIKE_with_igNOREe_case";
        createSimple(name, 155);
        createSimple(nameLike, 166);

        // when
        List<Simple> result1 = repo.queryByIgnoreCase(name.toLowerCase(), "no_match");
        List<Simple> result2 = repo.queryByIgnoreCase("no_match", "%" + nameLike.substring(5, 22) + "%");

        // then
        assertEquals(1, result1.size());
        assertEquals(Integer.valueOf(155), result1.get(0).getCounter());
        assertEquals(1, result2.size());
        assertEquals(Integer.valueOf(166), result2.get(0).getCounter());
    }

    @Test
    public void should_create_join_criteria_query()
    {
        // given
        final String name = "testCreateJoinCriteriaQuery";
        final String nameOne = name + "-one";
        final String nameMany = name + "-many";
        Parent parent = new Parent(name);
        parent.setOne(new OneToOne(nameOne));
        parent.add(new OneToMany(nameMany));

        getEntityManager().persist(parent);
        getEntityManager().flush();

        // when
        List<Parent> result = parentRepo.joinQuery(name, nameOne, nameMany);

        // then
        assertEquals(1, result.size());
        assertNotNull(result.get(0));

        Parent queried = result.get(0);
        assertEquals(name, queried.getName());
        assertNotNull(queried.getOne());
        assertEquals(nameOne, queried.getOne().getName());
        assertEquals(1, queried.getMany().size());
        assertEquals(nameMany, queried.getMany().get(0).getName());
    }

    @Test
    public void should_create_or_query()
    {
        // given
        final String name = "testCreateOrQuery";
        Parent parent1 = new Parent(name + "1");
        parent1.setValue(25L);
        Parent parent2 = new Parent(name + "2");
        parent2.setValue(75L);
        Parent parent3 = new Parent(name + "3");
        parent3.setValue(25L);
        Parent parent4 = new Parent(name + "1");
        parent4.setValue(75L);

        getEntityManager().persist(parent1);
        getEntityManager().persist(parent2);
        getEntityManager().persist(parent3);
        getEntityManager().persist(parent4);
        getEntityManager().flush();

        // when
        List<Parent> result = parentRepo.orQuery(name + "1", name + "2");

        // then
        assertEquals(2, result.size());
    }

    @Test
    public void should_create_ordered_query()
    {
        // given
        final String name = "testCreateOrderedQuery";
        Parent parent1 = new Parent(name + "99");
        Parent parent2 = new Parent(name + "12");
        Parent parent3 = new Parent(name + "19");
        Parent parent4 = new Parent(name + "02");

        getEntityManager().persist(parent1);
        getEntityManager().persist(parent2);
        getEntityManager().persist(parent3);
        getEntityManager().persist(parent4);
        getEntityManager().flush();

        // when
        List<Parent> result = parentRepo.orderedQuery();

        // then
        assertEquals(4, result.size());
        assertEquals(name + "02", result.get(0).getName());
        assertEquals(name + "12", result.get(1).getName());
        assertEquals(name + "19", result.get(2).getName());
        assertEquals(name + "99", result.get(3).getName());
    }

    @Test
    public void should_create_query_wihtout_nulls()
    {
        // given
        final String name = "testCreateQueryWihtoutNulls";
        Parent parent = new Parent(name);

        getEntityManager().persist(parent);
        getEntityManager().flush();

        // when
        List<Parent> result = parentRepo.nullAwareQuery(name, null, null);

        // then
        assertEquals(1, result.size());
        assertEquals(name, result.get(0).getName());
    }

    @Test
    public void should_create_fetch_query()
    {
        // given
        final String name = "testCreateFetchQuery";
        Parent parent = new Parent(name);
        parent.add(new OneToMany(name + "-1"));
        parent.add(new OneToMany(name + "-2"));

        getEntityManager().persist(parent);
        getEntityManager().flush();

        // when
        Parent result = parentRepo.fetchQuery(name);

        // then
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertNotNull(result.getMany());
        assertEquals(2, result.getMany().size());
    }

    @Test
    public void should_create_in_query()
    {
        // given
        final String name = "testCreateInQuery";
        Parent parent1 = new Parent(name + "-1");
        Parent parent2 = new Parent(name + "-2");
        Parent parent3 = new Parent(name + "-3");

        getEntityManager().persist(parent1);
        getEntityManager().persist(parent2);
        getEntityManager().persist(parent3);
        getEntityManager().flush();

        // when
        List<Parent> result = parentRepo.fetchByName(name + "-1", name + "-2", name + "-3");

        // then
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    public void should_create_select_criteria_with_result_type()
    {
        // given
        final String name = "testCreateSelectCriteriaWithResultType";
        createSimple(name, 1);
        createSimple(name, 2);
        createSimple(name, 3);
        createSimple(name, 4);
        createSimple(name, 99);

        // when
        Statistics result = repo.queryWithSelect(name);

        // then
        assertNotNull(result.getAverage());
        assertEquals(Long.valueOf(5l), result.getCount());
    }

    @Test
    public void should_create_select_criteria_without_result_type()
    {
        // given
        final String name = "testCreateSelectCriteriaWithoutResultType";
        createSimple(name, 10);
        createSimple(name, 99);

        // when
        Object[] result = repo.queryWithSelectAggregateReturnArray(name);

        // then
        assertEquals(Integer.valueOf(10), result[0]);
        assertEquals(Integer.valueOf(99), result[1]);
        assertTrue(result[2] instanceof java.sql.Date);
        assertTrue(result[3] instanceof java.sql.Time);
        assertTrue(result[4] instanceof java.sql.Timestamp);
    }

    @Test
    public void should_create_select_criteria_with_attributes()
    {
        // given
        final String name = "testCreateSelectCriteriaWithAttributes";
        createSimple(name, 10);
        createSimple(name, 99);

        // when
        List<Object[]> results = repo.queryWithSelectAttributes(name);

        // then
        for (Object[] result : results)
        {
            assertEquals(name, result[0]);
            assertEquals(name.toUpperCase(), result[1]);
            assertEquals(name.toLowerCase(), result[2]);
            assertEquals(name.substring(1), result[3]);
            assertEquals(name.substring(1, 1 + 2), result[4]);
        }
    }

    @Test
    public void should_create_select_criteria_with_optional_result()
    {
        // given
        final String name = "should_create_select_criteria_with_optional_result";
        createSimple(name, 10);

        // when
        Simple result1 = repo.queryOptional(name);
        Simple result2 = repo.queryOptional(name + "_doesnt exist");

        // then
        assertNotNull(result1);
        assertEquals(name, result1.getName());
        assertNull(result2);
    }

    @Test(expected = NonUniqueResultException.class)
    public void should_fail_with_optional_nonunique_result()
    {
        // given
        final String name = "should_fail_with_optional_nonunique_result";
        createSimple(name, 10);
        createSimple(name, 10);

        // when
        repo.queryOptional(name);

    }

    @Test
    public void should_create_select_criteria_with_any_result()
    {
        // given
        final String name = "should_create_select_criteria_with_any_result";
        createSimple(name, 10);
        createSimple(name, 10);

        // when
        Simple result1 = repo.queryAny(name);
        Simple result2 = repo.queryAny(name + "_doesnt exist");

        // then
        assertNotNull(result1);
        assertEquals(name, result1.getName());
        assertNull(result2);
    }

    @Test // SELECT COUNT(DISTINCT(s.name)) FROM Simple s WHERE s.name = 'should_create_count_criteria'
    public void should_create_count_criteria()
    {
        // given
        final String name = "should_create_count_criteria";
        createSimple(name, 10);
        createSimple(name, 11);

        // when
        Long result = repo.criteriaCount(name);

        // then
        assertNotNull(result);
        assertEquals(1l, result.longValue());
    }

    @Test
    public void should_create_date_criteria()
    {
        // given
        final String name = "should_create_date_criteria";
        final Simple simple = new Simple(name);
        simple.setTemporal(new Date());
        getEntityManager().persist(simple);
        getEntityManager().flush();

        Calendar cal = Calendar.getInstance();
        cal.setTime(simple.getTemporal());
        cal.add(Calendar.MINUTE, -1);
        Date from = cal.getTime();
        cal.add(Calendar.MINUTE, 2);
        Date to = cal.getTime();

        // when
        final List<Simple> result = repo.findByTimeBetween(from, to);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void should_query_with_att_from_mapped_super()
    {
        // given
        final String name = "should_create_date_criteria";
        final String superName = "super_should_create_date_criteria";
        final Simple simple = new Simple(name);
        simple.setSuperName(superName);
        getEntityManager().persist(simple);
        getEntityManager().flush();

        // when
        final Simple result = repo.findBySuperName(superName);

        // then
        assertEquals(superName, result.getSuperName());
    }

    @Test
    public void should_apply_multiply_orderby()
    {
        // given
        createSimple("a", 1);
        createSimple("b", 2);

        // when
        final List<Simple> orderByNameAndCounter = repo.findOrderByNameAndCounter();

        // then
        assertEquals(new Integer(2), orderByNameAndCounter.get(0).getCounter());
        assertEquals(new Integer(1), orderByNameAndCounter.get(1).getCounter());
    }

    @Test
    public void should_apply_trim()
    {
        // given
        final String name = " should_apply_trim ";
        createSimple(name, 10);

        // when
        Object[] objects = repo.queryWithSelectAttributesAndTrim(name);
        assertNotNull(objects);
        assertEquals(name, objects[0]);
        assertEquals(name.trim(), objects[1]);
        assertEquals("should_apply_trim ", objects[2]);
    }

    private Simple createSimple(String name, Integer counter)
    {
        Simple result = new Simple(name);
        result.setCounter(counter);
        getEntityManager().persist(result);
        getEntityManager().flush();
        return result;
    }
}
