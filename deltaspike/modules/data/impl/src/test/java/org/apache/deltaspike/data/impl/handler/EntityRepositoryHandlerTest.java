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

import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.domain.Simple2;
import org.apache.deltaspike.data.test.domain.SimpleStringId;
import org.apache.deltaspike.data.test.domain.Simple_;
import org.apache.deltaspike.data.test.service.ExtendedRepositoryAbstract;
import org.apache.deltaspike.data.test.service.ExtendedRepositoryAbstract2;
import org.apache.deltaspike.data.test.service.ExtendedRepositoryAbstract4;
import org.apache.deltaspike.data.test.service.ExtendedRepositoryInterface;
import org.apache.deltaspike.data.test.service.SimpleIntermediateRepository;
import org.apache.deltaspike.data.test.service.SimpleStringIdRepository;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import jakarta.inject.Inject;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.Optional;

import static org.apache.deltaspike.data.test.util.TestDeployments.initDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@Category(WebProfileCategory.class)
public class EntityRepositoryHandlerTest extends TransactionalTestCase
{

    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment()
                .addClasses(ExtendedRepositoryInterface.class)
                .addClasses(ExtendedRepositoryAbstract.class)
                .addClasses(ExtendedRepositoryAbstract2.class)
                .addClasses(ExtendedRepositoryAbstract4.class)
                .addClasses(SimpleStringIdRepository.class, SimpleIntermediateRepository.class)
                .addPackage(Simple.class.getPackage());
    }

    @Inject
    private ExtendedRepositoryInterface repo;

    @Inject
    private ExtendedRepositoryAbstract repoAbstract;

    @Inject
    private ExtendedRepositoryAbstract2 repoAbstract2;

    @Inject
    private ExtendedRepositoryAbstract4 repoAbstract4;

    @Inject
    private SimpleStringIdRepository stringIdRepo;

    @Inject
    private SimpleIntermediateRepository intermediate;

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
    public void should_save_with_string_id()
    {
        // given
        SimpleStringId foo = new SimpleStringId("foo", "bar");

        // when
        foo = stringIdRepo.save(foo);

        // then
        assertNotNull(foo);
    }


    @Test
    public void should_refresh() throws Exception
    {
        // given
        final String name = "testRefresh";
        Simple simple = testData.createSimple(name);

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
        Simple simple = testData.createSimple("testFindByPk");

        // when
        Simple find = repo.findBy(simple.getId());

        // then
        assertEquals(simple.getName(), find.getName());
    }
    
    @Test
    public void should_find__by_pk() throws Exception
    {
        // given
        Simple simple = testData.createSimple("testFindByPk");

        // when
        Optional<Simple> find = repo.findOptionalBy(simple.getId());

        // then
        assertEquals(simple.getName(), find.get().getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_find_by_example() throws Exception
    {
        // given
        Simple simple = testData.createSimple("testFindByExample");

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
        Simple simple = testData.createSimple("testFindByExample1", Integer.valueOf(10));
        testData.createSimple("testFindByExample1", Integer.valueOf(10));

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
        Simple simple = testData.createSimple("testFindByExample");
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
        testData.createSimple("testFindAll1");
        testData.createSimple("testFindAll2");

        // when
        List<Simple> find = repo.findAll();

        // then
        assertEquals(2, find.size());
    }

    @Test
    public void should_find_by_all_with_start_and_max()
    {
        // given
        testData.createSimple("testFindAll1");
        testData.createSimple("testFindAll2");

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
        testData.createSimple("testFindAll1");
        testData.createSimple("testFindAll2");
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
        testData.createSimple("testFindAll1");
        testData.createSimple("testFindAll2");
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
        testData.createSimple("testFindAll1", 1);
        testData.createSimple("testFindAll2", 2);
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
        testData.createSimple("testCountAll");

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
        Simple simple = testData.createSimple("testFindAll1", Integer.valueOf(55));
        testData.createSimple("testFindAll2", Integer.valueOf(55));

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
        Simple simple = testData.createSimple("testFindAll1");
        testData.createSimple("testFindAll2");
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
        testData.createSimple("testFindAll1");
        testData.createSimple("testFindAll2");
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
        Simple simple = testData.createSimple("testRemove");

        // when
        repo.remove(simple);
        repo.flush();
        Simple lookup = getEntityManager().find(Simple.class, simple.getId());

        // then
        assertNull(lookup);
    }

    @Test
    public void should_remove_and_flush() {
        // given
        Simple simple = testData.createSimple("testRemoveAndFlush");

        // when
        repo.removeAndFlush(simple);
        Simple lookup = getEntityManager().find(Simple.class, simple.getId());

        // then
        assertNull(lookup);
    }

    @Test
    public void should_remove_detach_entity() {
        //given
        Simple simple = testData.createSimple("testeAttachAndRemove");

        //when
        repo.detach(simple);
        repo.attachAndRemove(simple);
        repo.flush();
        Simple lookup = getEntityManager().find(Simple.class, simple.getId());

        // then
        assertNull(lookup);
    }

    @Test
    public void should_return_table_name()
    {
        final String tableName = repoAbstract.getTableName();
        final String tableName2 = repoAbstract2.getTableName();

        assertEquals("SIMPLE_TABLE", tableName);
        assertEquals(Simple2.class.getSimpleName(), tableName2);
    }

    @Test
    public void should_return_entity_name()
    {
        final String entityName = repoAbstract.getEntityName();
        final String entityName2 = repoAbstract4.getEntityName();

        assertEquals("Simple", entityName);
        assertEquals("EntitySimple4", entityName2);
    }

    @Test
    public void should_return_entity_primary_key()
    {
        //given
        Simple simple = testData.createSimple("should_return_entity_primary_key");
        Long id = simple.getId();

        //when
        Long primaryKey = repo.getPrimaryKey(simple);

        // then
        assertNotNull(primaryKey);
        assertEquals(id, primaryKey);
    }

    @Test
    public void should_return_null_primary_key()
    {
        //given
        Simple simple = new Simple("should_return_null_primary_key");

        //when
        Long primaryKey = repo.getPrimaryKey(simple);

        // then
        assertNull(primaryKey);
    }

    @Test
    public void should_return_entity_primary_key_detached_entity()
    {
        //given
        Simple simple = testData.createSimple("should_return_entity_primary_key");
        Long id = simple.getId();

        //when
        getEntityManager().detach(simple);
        Long primaryKey = repo.getPrimaryKey(simple);

        // then
        assertNotNull(primaryKey);
        assertEquals(id, primaryKey);
    }

    @Test
    public void should_query_with_hints()
    {
        Simple simple = testData.createSimple("should_return_entity_primary_key");
        Long id = simple.getId();

        getEntityManager().flush();
        getEntityManager().clear();

        Simple found = intermediate.findBy(id);

        assertEquals(id, found.getId());
    }

    @Test
    public void should_query_names()
    {
        String name = "should_return_entity_primary_key";
        testData.createSimple(name);

        List<String> names = intermediate.findAllNames();

        assertEquals(name, names.get(0));
    }

    @Test
    public void should_query_by_name()
    {
        String name = "should_return_entity_primary_key";
        Simple simple = testData.createSimple(name);

        Simple byName = stringIdRepo.findByName(name);

        assertEquals(simple, byName);
    }

    @Test
    public void should_query_list_by_name()
    {
        String name = "should_return_entity_primary_key";
        Simple simple = testData.createSimple(name);

        List<Simple> byName = stringIdRepo.findByName2(name);

        assertEquals(byName.size(), 1);
        assertEquals(simple, byName.get(0));
    }
}
