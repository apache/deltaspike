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

import static org.apache.deltaspike.data.test.util.TestDeployments.initDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.apache.deltaspike.data.test.TransactionalTestCase;
import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.domain.Simple_;
import org.apache.deltaspike.data.test.domain.dto.BooleanWrapper;
import org.apache.deltaspike.data.test.domain.dto.SimpleDto;
import org.apache.deltaspike.data.test.domain.dto.SimpleId;
import org.apache.deltaspike.data.test.service.SimpleMappedDtoRepository;
import org.apache.deltaspike.data.test.service.SimpleMappedRepository;
import org.apache.deltaspike.data.test.service.SimpleMapper;
import org.apache.deltaspike.data.test.service.SimpleQueryInOutMapper;
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
        return initDeployment()
                .addClasses(
                        SimpleMappedRepository.class,
                        SimpleMappedDtoRepository.class,
                        SimpleMapper.class,
                        WrappedMapper.class,
                        SimpleQueryInOutMapper.class)
                .addPackages(false,
                        Simple.class.getPackage(),
                        SimpleDto.class.getPackage());
    }

    @Inject
    private SimpleMappedRepository repository;

    @Inject
    private SimpleMappedDtoRepository dtoRepository;

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
        Simple loaded = getEntityManager().find(Simple.class, saved.getId().getId());

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
        getEntityManager().persist(simple);

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
        getEntityManager().persist(simple);

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
        getEntityManager().persist(simple);

        // when
        List<SimpleDto> result = repository.findByNameToo(name)
                .changeOrder(Simple_.name.getName())
                .getResultList();

        // then
        assertTrue(result.size() > 0);
    }

    @Test
    public void should_save_new_entity_with_simplemapper()
    {
        // given
        SimpleDto dto = new SimpleDto();
        dto.setName("should_save_new_entity_with_simplemapper");
        dto.setEnabled(Boolean.TRUE);

        // when
        SimpleDto result = dtoRepository.save(dto);

        // then
        assertNotNull(result);
        assertNotNull(result.getId());
    }

    @Test
    public void should_update_existing_entity_with_simplemapper()
    {
        // given
        final String name = "should_update_existing_entity_with_simplemapper";
        Simple simple = new Simple(name);
        simple.setEnabled(Boolean.TRUE);
        getEntityManager().persist(simple);

        SimpleDto dto = new SimpleDto();
        dto.setName(name + "_updated");
        dto.setEnabled(Boolean.TRUE);
        dto.setId(new SimpleId(simple.getId()));

        // when
        dtoRepository.save(dto);
        Simple lookup = getEntityManager().find(Simple.class, simple.getId());

        // then
        assertNotNull(lookup);
        assertEquals(name + "_updated", lookup.getName());
    }

}
