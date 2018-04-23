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
package org.apache.deltaspike.data.impl.handler;

import static org.apache.deltaspike.data.test.util.TestDeployments.initDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;

import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.domain.Simple2;
import org.apache.deltaspike.data.test.domain.SimpleBuilder;
import org.apache.deltaspike.data.test.service.Simple2Repository;
import org.apache.deltaspike.data.test.service.SimpleRepository;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(WebProfileCategory.class)
public class QueryHandlerTest extends TransactionalTestCase
{

    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment()
                .addClasses(SimpleRepository.class, Simple2Repository.class)
                .addPackage(Simple.class.getPackage());
    }

    @Inject
    private SimpleRepository repo;

    @Inject
    private Simple2Repository repo2;

    private SimpleBuilder builder;

    @Test
    public void should_delegate_to_implementation()
    {
        // given
        final String name = "testDelegateToImplementation";
        builder.createSimple(name);

        // when
        List<Simple> result = repo.implementedQueryByName(name);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void should_create_named_query_index()
    {
        // given
        final String name = "testCreateNamedQueryIndex";
        builder.createSimple(name);

        // when
        List<Simple> result = repo.findByNamedQueryIndexed(name, Boolean.TRUE);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(name, result.get(0).getName());
    }

    @Test
    public void should_create_named_query_named()
    {
        // given
        final String name = "testCreateNamedQueryNamed";
        Simple simple = builder.createSimple(name);

        // when
        Simple result = repo.findByNamedQueryNamed(simple.getId(), Boolean.TRUE);

        // then
        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    @Test
    public void should_run_annotated_query()
    {
        // given
        final String name = "testRunAnnotatedQuery";
        builder.createSimple(name);

        // when
        Simple result = repo.findByQuery(name);

        // then
        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    @Test
    public void should_create_query_by_method_name()
    {
        // given
        final String name = "testCreateQueryByMethodName";
        builder.createSimple(name);

        // when
        Simple result = repo.findByNameAndEnabled(name, Boolean.TRUE);

        // then
        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    @Test
    public void should_create_query_delete_by_method_name()
    {
        // given
        final String name = "testCreateQueryByMethodName";
        builder.createSimple(name);

        // when
        repo.deleteByName(name);
        repo.flush();
        Simple result = repo.findAnyByName(name);

        // then
        assertNull(result);
    }

    @Test
    public void should_create_query_remove_by_method_name()
    {
        // given
        final String name = "testCreateQueryByMethodName";
        builder.createSimple(name);

        // when
        repo.removeByName(name);
        repo.flush();
        Simple result = repo.findAnyByName(name);

        // then
        assertNull(result);
    }

    @Test
    public void should_create_query_remove_by_method_name_with_multiply_params()
    {
        // given
        final String name = "testCreateQueryByMethodName";
        builder.createSimple(name);

        // when
        repo.removeByNameAndEnabled(name, Boolean.TRUE);
        repo.flush();
        Simple result = repo.findAnyByName(name);

        // then
        assertNull(result);
    }

    @Test
    public void should_create_query_delete_by_method_name_with_multiply_params()
    {
        // given
        final String name = "testCreateQueryByMethodName";
        builder.createSimple(name);

        // when
        repo.deleteByNameAndEnabled(name, Boolean.TRUE);
        repo.flush();
        Simple result = repo.findAnyByName(name);

        // then
        assertNull(result);
    }

    @Test
    public void should_restrict_result_size_by_annotation()
    {
        // given
        final String name = "testRestrictResultSizeByAnnotation";
        builder.createSimple(name);
        builder.createSimple(name);

        // when
        List<Simple> result = repo.findByNamedQueryIndexed(name, Boolean.TRUE);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void should_restrict_result_size_by_parameters()
    {
        // given
        final String name = "testRestrictResultSizeByParameters";
        builder.createSimple(name);
        Simple second = builder.createSimple(name);

        // when
        List<Simple> result = repo.findByNamedQueryRestricted(name, Boolean.TRUE, 1, 1);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(second.getId(), result.get(0).getId());
    }

    @Test
    public void should_work_with_2nd_repo()
    {
        // given
        final String name = "testWorkWith2ndRepository";
        Simple2 simple = createSimple2(name);

        // when
        Simple2 result = repo2.findByName(name);

        // then
        assertNotNull(result);
        assertEquals(simple.getId(), result.getId());
        assertEquals(name, result.getName());
    }

    @Test
    public void should_return_aggregate()
    {
        // given
        final String name = "testReturnAggregate";
        builder.createSimple(name);

        // when
        Long result = repo.findCountByQuery(name);

        // then
        assertNotNull(result);
    }

    @Test
    public void should_find_with_native_query()
    {
        // given
        final String name = "testFindWithNativeQuery";
        builder.createSimple(name);
        builder.createSimple(name);

        // when
        List<Simple> result = repo.findWithNative(name);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0) instanceof Simple);
        assertEquals(name, result.get(0).getName());
    }

    @Test
    public void should_order_result_by_method_order_by()
    {
        // given
        final String name = "testFindWithNativeQuery";
        builder.createSimple(name, Integer.valueOf(33));
        builder.createSimple(name, Integer.valueOf(66));
        builder.createSimple(name, Integer.valueOf(66));
        builder.createSimple(name, Integer.valueOf(22));
        builder.createSimple(name, Integer.valueOf(55));

        // when
        List<Simple> result = repo.findByOrderByCounterAscIdDesc();

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        long lastId = Long.MAX_VALUE;
        int lastCounter = Integer.MIN_VALUE;
        for (Simple simple : result)
        {
            long currentId = simple.getId().longValue();
            int currentCounter = simple.getCounter().intValue();
            if (currentCounter == lastCounter)
            {
                assertTrue(currentId < lastId);
            }
            else
            {
                assertTrue(currentCounter > lastCounter);
            }
            lastId = currentId;
            lastCounter = currentCounter;
        }
    }

    @Test
    public void should_execute_update()
    {
        // given
        final String name = "testFindWithNativeQuery";
        final String newName = "testFindWithNativeQueryUpdated" + System.currentTimeMillis();
        Simple s = builder.createSimple(name);

        // when
        int count = repo.updateNameForId(newName, s.getId());

        // then
        assertEquals(1, count);
    }

    @Test
    public void should_create_optinal_query_by_name()
    {
        // given
        final String name = "should_create_optinal_query_by_name";
        builder.createSimple(name);

        // when
        Simple result1 = repo.findOptionalByName(name);
        Simple result2 = repo.findOptionalByName(name + "_doesnt_exist");

        // then
        assertNotNull(result1);
        assertEquals(name, result1.getName());
        assertNull(result2);
    }

    @Test
    public void should_create_optinal_query_by_annotation()
    {
        // given
        final String name = "should_create_optinal_query_by_annotation";
        builder.createSimple(name);

        // when
        Simple result1 = repo.findByNameOptional(name);
        Simple result2 = repo.findByNameOptional(name + "_doesnt_exist");

        // then
        assertNotNull(result1);
        assertEquals(name, result1.getName());
        assertNull(result2);
    }

    @Test(expected = NonUniqueResultException.class)
    public void should_fail_optinal_query_by_name_with_nonunique()
    {
        // given
        final String name = "should_fail_optinal_query_by_name_with_nonunique";
        builder.createSimple(name);
        builder.createSimple(name);

        // when
        repo.findOptionalByName(name);
    }

    @Test(expected = NonUniqueResultException.class)
    public void should_fail_optinal_query_by_annotation_with_nonunique()
    {
        // given
        final String name = "should_fail_optinal_query_by_annotation_with_nonunique";
        builder.createSimple(name);
        builder.createSimple(name);

        // when
        repo.findByNameOptional(name);
    }

    @Test
    public void should_create_any_query_by_name()
    {
        // given
        final String name = "should_create_any_query_by_name";
        builder.createSimple(name);
        builder.createSimple(name);

        // when
        Simple result1 = repo.findAnyByName(name);
        Simple result2 = repo.findAnyByName(name + "_doesnt_exist");

        // then
        assertNotNull(result1);
        assertEquals(name, result1.getName());
        assertNull(result2);
    }

    @Test
    public void should_create_any_query_by_annotation()
    {
        // given
        final String name = "should_create_any_query_by_annotation";
        builder.createSimple(name);
        builder.createSimple(name);

        // when
        Simple result1 = repo.findByNameAny(name);
        Simple result2 = repo.findByNameAny(name + "_doesnt_exist");

        // then
        assertNotNull(result1);
        assertEquals(name, result1.getName());
        assertNull(result2);
    }

    @Test
    public void should_create_case_insensitive_query_for_like_comparator()
    {
        // given
        final String name = "Should_Create_Case_Insensitive_Query_For_Like";
        builder.createSimple(name);

        // when
        Simple result = repo.findByNameLikeIgnoreCase("should_create_CASE_Insensitive_QUERY_for_l%");

        // then
        assertEquals(name, result.getName());
    }

    @Test
    public void should_create_case_insensitive_query_for_equals_comparator()
    {
        // given
        final String name = "Should_Create_Case_Insensitive_Query_for_Equals";
        builder.createSimple(name);

        // when
        Simple result = repo.findByNameIgnoreCase(name.toLowerCase());

        // then
        assertEquals(name, result.getName());
    }

    @Test
    public void should_find_first_2()
    {
        final String name = "Should_Create_Case_Insensitive_Query_for_Equals";
        builder.createSimple(name);
        builder.createSimple(name);
        builder.createSimple(name);
        builder.createSimple("this is something else");

        List<Simple> result = repo.findFirst2ByName(name);

        assertEquals(2, result.size());
    }

    @Test
    public void should_find_top_2()
    {
        final String name = "Should_Create_Case_Insensitive_Query_for_Equals";
        builder.createSimple(name);
        builder.createSimple(name);
        builder.createSimple(name);
        builder.createSimple("this is something else");

        List<Simple> result = repo.findTop2ByName(name);

        assertEquals(2, result.size());
    }

    @Test
    public void should_find_top_3_ordered()
    {
        builder.createSimple("zebra");
        builder.createSimple("willow");
        builder.createSimple("kangaroo");
        builder.createSimple("bologna");

        List<Simple> result = repo.findFirst3OrderByName();

        assertEquals("bologna", result.get(0).getName());
        assertEquals("kangaroo", result.get(1).getName());
        assertEquals("willow", result.get(2).getName());
    }

    @Test
    public void should_find_all_ordered()
    {
        builder.createSimple("zebra");
        builder.createSimple("willow");
        builder.createSimple("kangaroo");
        builder.createSimple("bologna");

        List<Simple> result = repo.findAllOrderByName();

        assertEquals("bologna", result.get(0).getName());
        assertEquals("kangaroo", result.get(1).getName());
        assertEquals("willow", result.get(2).getName());
        assertEquals("zebra", result.get(3).getName());
    }
    
    @Test
    public void should_count_by_name()
    {
        builder.createSimple("zebra");
        builder.createSimple("zebra");
        builder.createSimple("willow");
        builder.createSimple("kangaroo");
        builder.createSimple("kangaroo");
        builder.createSimple("kangaroo");
        builder.createSimple("bologna");

        assertEquals(repo.countByName("bologna"), 1);
        assertEquals(repo.countByName("kangaroo"), 3);
        assertEquals(repo.countByName("willow"), 1);
        assertEquals(repo.countByName("zebra"), 2);
    }

    @Before
    public void setup()
    {
        builder = new SimpleBuilder(getEntityManager());
    }

    private Simple2 createSimple2(String name)
    {
        Simple2 result = new Simple2(name);
        getEntityManager().persist(result);
        getEntityManager().flush();
        return result;
    }

}
