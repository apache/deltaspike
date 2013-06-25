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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.domain.Simple_;
import org.apache.deltaspike.data.test.service.ExtendedRepositoryInterface;
import org.apache.deltaspike.data.test.util.TestDeployments;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;

public class EntityRepositoryHandlerTest extends TransactionalTestCase
{

    @Deployment
    public static Archive<?> deployment()
    {
        return TestDeployments.initDeployment()
                .addClasses(ExtendedRepositoryInterface.class)
                .addPackage(Simple.class.getPackage());
    }

    @Inject
    private ExtendedRepositoryInterface repo;

    @Produces
    @PersistenceContext
    private EntityManager entityManager;

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
    public void should_merge() throws Exception
    {
        // given
        Simple simple = createSimple("testMerge");
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
    public void should_save_and_flush() throws Exception
    {
        // given
        Simple simple = new Simple("test");

        // when
        simple = repo.saveAndFlush(simple);
        Simple fetch = (Simple) entityManager
                .createNativeQuery("select * from simple_table where id = ?", Simple.class)
                .setParameter(1, simple.getId())
                .getSingleResult();

        // then
        assertEquals(simple.getId(), fetch.getId());
    }

    @Test
    public void should_refresh() throws Exception
    {
        // given
        final String name = "testRefresh";
        Simple simple = createSimple(name);

        // when
        simple.setName("override");
        repo.refresh(simple);

        // then
        assertEquals(name, simple.getName());
    }

    @Test
    public void should_find_by_pk() throws Exception
    {
        // given
        Simple simple = createSimple("testFindByPk");

        // when
        Simple find = repo.findBy(simple.getId());

        // then
        assertEquals(simple.getName(), find.getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_find_by_example() throws Exception
    {
        // given
        Simple simple = createSimple("testFindByExample");

        // when
        List<Simple> find = repo.findBy(simple, Simple_.name);

        // then
        assertNotNull(find);
        assertFalse(find.isEmpty());
        assertEquals(simple.getName(), find.get(0).getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_find_by_example_with_start_and_max() throws Exception
    {
        // given
        Simple simple = createSimple("testFindByExample1", Integer.valueOf(10));
        createSimple("testFindByExample1", Integer.valueOf(10));

        // when
        List<Simple> find = repo.findBy(simple, 0, 1, Simple_.name, Simple_.counter);

        // then
        assertNotNull(find);
        assertFalse(find.isEmpty());
        assertEquals(1, find.size());
        assertEquals(simple.getName(), find.get(0).getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_find_by_example_with_no_attributes() throws Exception
    {
        // given
        Simple simple = createSimple("testFindByExample");
        SingularAttribute<Simple, ?>[] attributes = new SingularAttribute[] {};

        // when
        List<Simple> find = repo.findBy(simple, attributes);

        // then
        assertNotNull(find);
        assertFalse(find.isEmpty());
        assertEquals(simple.getName(), find.get(0).getName());
    }

    @Test
    public void should_find_all()
    {
        // given
        createSimple("testFindAll1");
        createSimple("testFindAll2");

        // when
        List<Simple> find = repo.findAll();

        // then
        assertEquals(2, find.size());
    }

    @Test
    public void should_find_by_all_with_start_and_max()
    {
        // given
        createSimple("testFindAll1");
        createSimple("testFindAll2");

        // when
        List<Simple> find = repo.findAll(0, 1);

        // then
        assertEquals(1, find.size());
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void should_find_by_like()
    {
        // given
        createSimple("testFindAll1");
        createSimple("testFindAll2");
        Simple example = new Simple("test");

        // when
        List<Simple> find = repo.findByLike(example, Simple_.name);

        // then
        assertEquals(2, find.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_find_by_like_with_start_and_max()
    {
        // given
        createSimple("testFindAll1");
        createSimple("testFindAll2");
        Simple example = new Simple("test");

        // when
        List<Simple> find = repo.findByLike(example, 1, 10, Simple_.name);

        // then
        assertEquals(1, find.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_find_by_like_non_string()
    {
        // given
        createSimple("testFindAll1", 1);
        createSimple("testFindAll2", 2);
        Simple example = new Simple("test");
        example.setCounter(1);

        // when
        List<Simple> find = repo.findByLike(example, Simple_.name, Simple_.counter);

        // then
        assertEquals(1, find.size());
    }

    @Test
    public void should_count_all()
    {
        // given
        createSimple("testCountAll");

        // when
        Long result = repo.count();

        // then
        assertEquals(Long.valueOf(1), result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_count_with_attributes()
    {
        // given
        Simple simple = createSimple("testFindAll1", Integer.valueOf(55));
        createSimple("testFindAll2", Integer.valueOf(55));

        // when
        Long result = repo.count(simple, Simple_.name, Simple_.counter);

        // then
        assertEquals(Long.valueOf(1), result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_count_with_no_attributes()
    {
        // given
        Simple simple = createSimple("testFindAll1");
        createSimple("testFindAll2");
        SingularAttribute<Simple, Object>[] attributes = new SingularAttribute[] {};

        // when
        Long result = repo.count(simple, attributes);

        // then
        assertEquals(Long.valueOf(2), result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_count_by_like()
    {
        // given
        createSimple("testFindAll1");
        createSimple("testFindAll2");
        Simple example = new Simple("test");

        // when
        Long count = repo.countLike(example, Simple_.name);

        // then
        assertEquals(Long.valueOf(2), count);
    }

    @Test
    public void should_remove()
    {
        // given
        Simple simple = createSimple("testRemove");

        // when
        repo.remove(simple);
        repo.flush();
        Simple lookup = entityManager.find(Simple.class, simple.getId());

        // then
        assertNull(lookup);
    }

    @Override
    protected EntityManager getEntityManager()
    {
        return entityManager;
    }

    private Simple createSimple(String name)
    {
        return createSimple(name, null);
    }

    private Simple createSimple(String name, Integer counter)
    {
        Simple result = new Simple(name);
        result.setCounter(counter);
        entityManager.persist(result);
        entityManager.flush();
        return result;
    }

}
