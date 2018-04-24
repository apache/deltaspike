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

package org.apache.deltaspike.data.test.java8.test;

import org.apache.deltaspike.data.test.java8.entity.Simple;
import org.apache.deltaspike.data.test.java8.repo.SimpleRepository;
import org.apache.deltaspike.data.test.java8.repo.SimpleRepository2;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.List;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.deltaspike.data.test.java8.util.TestDeployments.initDeployment;

@Category(WebProfileCategory.class)
@RunWith(Arquillian.class)
public class Java8Test
{
    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment()
                .addClasses(Java8Test.class, Simple.class, SimpleRepository.class, SimpleRepository2.class);
    }

    @Inject
    private SimpleRepository simpleRepository;

    @Inject
    private SimpleRepository2 simpleRepository2;

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private UserTransaction ut;

    @Before
    public void setupTX() throws Exception
    {
        ut.begin();
    }

    @After
    public void rollbackTX() throws Exception
    {
        ut.rollback();
    }

    @Test
    public void shouldFindOptionalSimple() throws Exception
    {
        Simple s = new Simple("something");
        entityManager.persist(s);

        Optional<Simple> found = simpleRepository.findOptionalBy(s.getId());

        Assert.assertTrue(found.isPresent());
    }

    @Test
    public void shouldNotFindOptionalSimpleForMissing() throws Exception
    {
        Optional<Simple> found = simpleRepository.findBy(-1L);

        Assert.assertFalse(found.isPresent());
    }

    @Test
    public void shouldFindStreamOfSimples()
    {
        String name = "something";
        Simple s = new Simple(name);
        entityManager.persist(s);

        Stream<Simple> found = simpleRepository.findByName(name);

        Assert.assertEquals(1, found.count());
    }

    @Test
    public void shouldFindEmptyStream()
    {
        String name = "something";
        Simple s = new Simple(name);
        entityManager.persist(s);

        Stream<Simple> found = simpleRepository.findByName("some other name");

        Assert.assertEquals(emptyList(), found.collect(toList()));
    }

    @Test
    public void shouldFindAllAsStream()
    {
        String name = "something";
        Simple s = new Simple(name);
        entityManager.persist(s);

        Stream<Simple> found = simpleRepository.findAll();

        Assert.assertEquals(1, found.count());
    }

    @Test
    public void shouldFindByNameOptional()
    {
        String name = "jim";
        entityManager.persist(new Simple(name));
        entityManager.persist(new Simple(name));

        Optional<Simple> found = simpleRepository2.findByName(name);

        Assert.assertTrue(found.isPresent());
    }

    @Test
    public void shouldFindNamesAsStream()
    {
        entityManager.persist(new Simple("a"));
        entityManager.persist(new Simple("b"));
        entityManager.flush();

        Stream<String> names = simpleRepository2.findSimpleNames();
        final List<String> actualSorted = names.collect(toList());
        Collections.sort(actualSorted);

        Assert.assertEquals(asList("a","b"), actualSorted);
    }
}
