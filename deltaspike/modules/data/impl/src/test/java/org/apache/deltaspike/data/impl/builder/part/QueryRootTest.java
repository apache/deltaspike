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

import static org.junit.Assert.assertEquals;

import org.apache.deltaspike.data.impl.builder.MethodExpressionException;
import org.apache.deltaspike.data.impl.meta.RepositoryComponent;
import org.apache.deltaspike.data.impl.meta.RepositoryEntity;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.service.SimpleFetchRepository;
import org.apache.deltaspike.data.test.service.SimpleRepository;
import org.junit.Test;

/**
 *
 * @author thomashug
 */
public class QueryRootTest
{

    private final RepositoryComponent repo = new RepositoryComponent(SimpleRepository.class, new RepositoryEntity(Simple.class, Long.class));
    private final RepositoryComponent repoFetchBy = new RepositoryComponent(SimpleFetchRepository.class, new RepositoryEntity(Simple.class, Long.class));

    @Test
    public void should_create_simple_query()
    {
        // given
        final String name = "findByName";
        final String expected =
                "select e from Simple e " +
                        "where e.name = ?1";

        // when
        String result = QueryRoot.create(name, repo).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test
    public void should_create_complex_query()
    {
        // given
        final String name = "findByNameAndTemporalBetweenOrEnabledIsNull" +
                "AndCamelCaseAndEmbedded_embeddNotEqualOrderByEmbedded_embeddDesc";
        final String expected =
                "select e from Simple e " +
                        "where e.name = ?1 " +
                        "and e.temporal between ?2 and ?3 " +
                        "or e.enabled IS NULL " +
                        "and e.camelCase = ?4 " +
                        "and e.embedded.embedd <> ?5 " +
                        "order by e.embedded.embedd desc";

        // when
        String result = QueryRoot.create(name, repo).getJpqlQuery().trim();

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
        String result = QueryRoot.create(name, repo).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

    @Test(expected = MethodExpressionException.class)
    public void should_fail_in_where()
    {
        // given
        final String name = "findByInvalid";

        // when
        QueryRoot.create(name, repo);
    }

    @Test(expected = MethodExpressionException.class)
    public void should_fail_with_prefix_only()
    {
        // given
        final String name = "findBy";

        // when
        QueryRoot.create(name, repo);
    }

    @Test(expected = MethodExpressionException.class)
    public void should_fail_in_order_by()
    {
        // given
        final String name = "findByNameOrderByInvalidDesc";

        // when
        QueryRoot.create(name, repo);
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
        String result = QueryRoot.create(name, repoFetchBy).getJpqlQuery().trim();

        // then
        assertEquals(expected, result);
    }

}
