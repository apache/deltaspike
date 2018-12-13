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
package org.apache.deltaspike.data.impl.builder.part;

import org.apache.deltaspike.data.impl.builder.MethodExpressionException;
import org.apache.deltaspike.data.impl.meta.EntityMetadata;
import org.apache.deltaspike.data.impl.meta.RepositoryMetadata;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodPrefix;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.service.SimpleFetchRepository;
import org.apache.deltaspike.data.test.service.SimpleRepository;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QueryRootTest
{
    private final RepositoryMetadata repo = new RepositoryMetadata(SimpleRepository.class, new EntityMetadata(Simple.class, "Simple", Long.class));
    private final RepositoryMetadata repoFetchBy = new RepositoryMetadata(SimpleFetchRepository.class, new EntityMetadata(Simple.class, "Simple", Long.class));

    @Test
    public void should_create_simple_query()
    {
        // given
        final String name = "findByName";
        final String expected =
                "select e from Simple e " +
                        "where e.name = ?1";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_create_complex_query()
    {
        // given
        final String name = "findByNameAndTemporalBetweenOrEnabledIsNull" +
                "AndCamelCaseLikeIgnoreCaseAndEmbedded_embeddNotEqualIgnoreCase" +
                "OrderByEmbedded_embeddDesc";
        final String expected =
                "select e from Simple e " +
                        "where e.name = ?1 " +
                        "and e.temporal between ?2 and ?3 " +
                        "or e.enabled IS NULL " +
                        "and upper(e.camelCase) like ?4 " +
                        "and upper(e.embedded.embedd) <> upper(?5) " +
                        "order by e.embedded.embedd desc";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_create_query_with_order_by_only()
    {
        // given
        final String name = "findByOrderByIdAsc";
        final String expected =
                "select e from Simple e " +
                        "order by e.id asc";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test(expected = MethodExpressionException.class)
    public void should_fail_in_where()
    {
        // given
        final String name = "findByInvalid";

        // when
        QueryRoot.create(name, repo, prefix(name));
    }

    @Test(expected = MethodExpressionException.class)
    public void should_fail_with_prefix_only()
    {
        // given
        final String name = "findBy";

        // when
        QueryRoot.create(name, repo, prefix(name));
    }

    @Test(expected = MethodExpressionException.class)
    public void should_fail_in_order_by()
    {
        // given
        final String name = "findByNameOrderByInvalidDesc";

        // when
        QueryRoot.create(name, repo, prefix(name));
    }

    @Test
    public void should_use_alternative_prefix()
    {
        // given
        final String name = "fetchByName";
        final String expected =
                "select e from Simple e " +
                        "where e.name = ?1";

        // when
        String result = QueryRoot.create(name, repoFetchBy, new RepositoryMethodPrefix("fetchBy", name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_create_delete_query_by_name()
    {
        // given
        final String name = "deleteByName";
        final String expected =
                "delete from Simple e " +
                        "where e.name = ?1";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_create_delete_query_by_name_and_enabled()
    {
        // given
        final String name = "deleteByNameAndEnabled";
        final String expected =
                "delete from Simple e " +
                        "where e.name = ?1 and e.enabled = ?2";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_order_by_in_order()
    {
        // given
        final String name = "findAllOrderByNameDescIdAsc";
        final String expected =
                "select e from Simple e " +
                        "order by e.name desc, e.id asc";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_LessThan()
    {
        // given
        final String name = "findByNameLessThan";
        final String expected =
                "select e from Simple e " +
                        "where e.name < ?1";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_LessThanEquals()
    {
        // given
        final String name = "findByNameLessThanEquals";
        final String expected =
                "select e from Simple e " +
                        "where e.name <= ?1";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_GreaterThan()
    {
        // given
        final String name = "findByNameGreaterThan";
        final String expected =
                "select e from Simple e " +
                        "where e.name > ?1";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_GreaterThanEquals()
    {
        // given
        final String name = "findByNameGreaterThanEquals";
        final String expected =
                "select e from Simple e " +
                        "where e.name >= ?1";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_Like()
    {
        // given
        final String name = "findByNameLike";
        final String expected =
                "select e from Simple e " +
                        "where e.name like ?1";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_NotLike()
    {
        // given
        final String name = "findByNameNotLike";
        final String expected =
                "select e from Simple e " +
                        "where e.name not like ?1";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_LikeIgnoreCase()
    {
        // given
        final String name = "findByNameLikeIgnoreCase";
        final String expected =
                "select e from Simple e " +
                        "where upper(e.name) like ?1";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_NotEqual()
    {
        // given
        final String name = "findByNameNotEqual";
        final String expected =
                "select e from Simple e " +
                        "where e.name <> ?1";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_NotEqualIgnoreCase()
    {
        // given
        final String name = "findByNameNotEqualIgnoreCase";
        final String expected =
                "select e from Simple e " +
                        "where upper(e.name) <> upper(?1)";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_Equal()
    {
        // given
        final String name = "findByNameEqual";
        final String expected =
                "select e from Simple e " +
                        "where e.name = ?1";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_EqualIgnoreCase()
    {
        // given
        final String name = "findByNameEqualIgnoreCase";
        final String expected =
                "select e from Simple e " +
                        "where upper(e.name) = upper(?1)";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_IgnoreCase()
    {
        // given
        final String name = "findByNameIgnoreCase";
        final String expected =
                "select e from Simple e " +
                        "where upper(e.name) = upper(?1)";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_In()
    {
        // given
        final String name = "findByNameIn";
        final String expected =
                "select e from Simple e " +
                        "where e.name IN ?1";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_Between()
    {
        // given
        final String name = "findByNameBetween";
        final String expected =
                "select e from Simple e " +
                        "where e.name between ?1 and ?2";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_IsNotNull()
    {
        // given
        final String name = "findByNameIsNotNull";
        final String expected =
                "select e from Simple e " +
                        "where e.name IS NOT NULL";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_IsNull()
    {
        // given
        final String name = "findByNameIsNull";
        final String expected =
                "select e from Simple e " +
                        "where e.name IS NULL";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_NotIn()
    {
        // given
        final String name = "findByNameNotIn";
        final String expected =
                "select e from Simple e " +
                        "where e.name NOT IN ?1";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_True()
    {
        // given
        final String name = "findByNameTrue";
        final String expected =
                "select e from Simple e " +
                        "where e.name IS TRUE";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_False()
    {
        // given
        final String name = "findByNameFalse";
        final String expected =
                "select e from Simple e " +
                        "where e.name IS FALSE";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_Containing()
    {
        // given
        final String name = "findByNameContaining";
        final String expected =
                "select e from Simple e " +
                        "where e.name like CONCAT('%', CONCAT(?1, '%'))";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_StartingWith()
    {
        // given
        final String name = "findByNameStartingWith";
        final String expected =
                "select e from Simple e " +
                        "where e.name like CONCAT(?1, '%')";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_apply_comparator_EndingWith()
    {
        // given
        final String name = "findByNameEndingWith";
        final String expected =
                "select e from Simple e " +
                        "where e.name like CONCAT('%', ?1)";

        // when
        String result = QueryRoot.create(name, repo, prefix(name)).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    private RepositoryMethodPrefix prefix(final String name)
    {
        return new RepositoryMethodPrefix("", name);
    }

}
