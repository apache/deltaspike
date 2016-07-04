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
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.service.FullRepositoryAbstract;
import org.apache.deltaspike.data.test.service.FullRepositoryInterface;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(WebProfileCategory.class)
public class FullEntityRepositoryTest extends TransactionalTestCase
{

    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment()
                .addClasses(FullRepositoryInterface.class)
                .addClasses(FullRepositoryAbstract.class)
                .addPackage(Simple.class.getPackage());
    }

    @Inject
    private FullRepositoryInterface repo;

    @Inject
    private FullRepositoryAbstract repoAbstract;

    @Test
    public void should_save() throws Exception
    {
        // given
        Simple simple = new Simple("test");

        // when
        simple = repo.save(simple);

        // then
        assertNotNull(simple.getId());
    }

    @Test
    public void should_save_abstract() throws Exception
    {
        // given
        Simple simple = new Simple("test");

        // when
        simple = repoAbstract.save(simple);

        // then
        assertNotNull(simple.getId());
    }

    @Test
    public void should_persist() throws Exception
    {
        // given
        Simple simple = new Simple("test");

        // when
        repo.persist(simple);

        // then
        assertNotNull(simple.getId());
    }

    @Test
    public void should_persist_abstract() throws Exception
    {
        // given
        Simple simple = new Simple("test");

        // when
        repoAbstract.persist(simple);

        // then
        assertNotNull(simple.getId());
    }

    @Test
    public void should_save_with_merge() throws Exception
    {
        // given
        Simple simple = testData.createSimple("testMerge");
        Long id = simple.getId();

        // when
        final String newName = "testMergeUpdated";
        simple.setName(newName);
        simple = repo.save(simple);

        // then
        assertEquals(id, simple.getId());
        assertEquals(newName, simple.getName());
    }

    @Test
    public void should_save_with_merge_abstract() throws Exception
    {
        // given
        Simple simple = testData.createSimple("testMerge");
        Long id = simple.getId();

        // when
        final String newName = "testMergeUpdated";
        simple.setName(newName);
        simple = repoAbstract.save(simple);

        // then
        assertEquals(id, simple.getId());
        assertEquals(newName, simple.getName());
    }

    @Test
    public void should_merge() throws Exception
    {
        // given
        Simple simple = testData.createSimple("testMerge");
        Long id = simple.getId();

        // when
        final String newName = "testMergeUpdated";
        simple.setName(newName);
        simple = repo.merge(simple);

        // then
        assertEquals(id, simple.getId());
        assertEquals(newName, simple.getName());
    }

    @Test
    public void should_merge_abstract() throws Exception
    {
        // given
        Simple simple = testData.createSimple("testMerge");
        Long id = simple.getId();

        // when
        final String newName = "testMergeUpdated";
        simple.setName(newName);
        simple = repoAbstract.merge(simple);

        // then
        assertEquals(id, simple.getId());
        assertEquals(newName, simple.getName());
    }

    @Test
    public void should_save_and_flush() throws Exception
    {
        // given
        Simple simple = new Simple("test");

        // when
        simple = repo.saveAndFlush(simple);
        Simple fetch = (Simple) getEntityManager()
                .createNativeQuery("select * from SIMPLE_TABLE where id = ?", Simple.class)
                .setParameter(1, simple.getId())
                .getSingleResult();

        // then
        assertEquals(simple.getId(), fetch.getId());
    }

    @Test
    public void should_find_by_criteria() throws Exception
    {
        // given
        Simple simple = new Simple("criteria");
        
        // when
        simple = repoAbstract.saveAndFlush(simple);
        Simple fetch = repoAbstract.fetchByName("criteria");

        // then
        assertEquals(simple.getId(), fetch.getId());
    }

}
