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
package org.apache.deltaspike.data.impl.mapping;

import static org.apache.deltaspike.data.test.util.TestDeployments.finalizeDeployment;
import static org.apache.deltaspike.data.test.util.TestDeployments.initDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.domain.Simple_;
import org.apache.deltaspike.data.test.domain.dto.BooleanWrapper;
import org.apache.deltaspike.data.test.domain.dto.SimpleDto;
import org.apache.deltaspike.data.test.service.SimpleMappedRepository;
import org.apache.deltaspike.data.test.service.SimpleMapper;
import org.apache.deltaspike.data.test.service.WrappedMapper;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(WebProfileCategory.class)
public class MappedRepositoryTest extends TransactionalTestCase
{

    @Deployment
    public static Archive<?> deployment()
    {
        return finalizeDeployment(MappedRepositoryTest.class,
                initDeployment()
                    .addClasses(
                            SimpleMappedRepository.class,
                            SimpleMapper.class,
                            WrappedMapper.class)
                    .addPackages(false,
                            Simple.class.getPackage(),
                            SimpleDto.class.getPackage()));
    }

    @Inject
    private SimpleMappedRepository repository;

    @Produces
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void should_map_entityrepo_methods()
    {
        // given
        SimpleDto dto = new SimpleDto();
        dto.setName("should_map_entityrepo_methods");
        dto.setEnabled(Boolean.TRUE);

        // when
        SimpleDto saved = repository.saveAndFlush(dto);
        SimpleDto loadedDto = repository.findBy(saved.getId());
        Simple loaded = entityManager.find(Simple.class, saved.getId().getId());

        // then
        assertNotNull(loadedDto);
        assertNotNull(loaded);
        assertEquals(saved.getName(), loaded.getName());
        assertEquals(saved.getEnabled(), loaded.getEnabled());
    }

    @Test
    public void should_map_method_expression()
    {
        // given
        Simple simple = new Simple("should_map_method_expression");
        simple.setEnabled(Boolean.TRUE);
        entityManager.persist(simple);

        // when
        List<SimpleDto> result = repository.findByEnabled(Boolean.TRUE);

        // then
        boolean found = false;
        for (SimpleDto dto : result)
        {
            if (dto.getName().equals(simple.getName()))
            {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void should_override_class_config_with_method_config()
    {
        // given
        Simple simple = new Simple("should_map_method_expression");
        simple.setEnabled(Boolean.TRUE);
        entityManager.persist(simple);

        // when
        List<SimpleDto> result = repository.findByEnabled(new BooleanWrapper(Boolean.TRUE));

        // then
        boolean found = false;
        for (SimpleDto dto : result)
        {
            if (dto.getName().equals(simple.getName()))
            {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void should_find_with_queryresult()
    {
        // given
        final String name = "should_find_with_queryresult";
        Simple simple = new Simple(name);
        simple.setEnabled(Boolean.TRUE);
        entityManager.persist(simple);

        // when
        List<SimpleDto> result = repository.findByNameToo(name)
                .changeOrder(Simple_.name.getName())
                .getResultList();

        // then
        assertTrue(result.size() > 0);
    }

    @Override
    protected EntityManager getEntityManager()
    {
        return entityManager;
    }

}
