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
package org.apache.deltaspike.data.impl.meta.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.deltaspike.data.impl.meta.RepositoryEntity;
import org.apache.deltaspike.data.test.domain.Parent;
import org.apache.deltaspike.data.test.domain.TeeId;
import org.apache.deltaspike.data.test.domain.mapped.MappedOne;
import org.apache.deltaspike.data.test.domain.mapped.MappedThree;
import org.apache.deltaspike.data.test.domain.mapped.MappedTwo;
import org.apache.deltaspike.data.test.service.MappedOneRepository;
import org.apache.deltaspike.data.test.util.TestDeployments;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class PersistenceUnitsTest
{

    @Deployment
    public static Archive<?> deployment()
    {
        WebArchive war = TestDeployments.initDeployment();
        
        war.delete("WEB-INF/classes/META-INF/persistence.xml");
        
        return war.addPackages(true, Parent.class.getPackage())
                .addClasses(MappedOneRepository.class)
                .addAsWebInfResource("test-mapped-persistence.xml",
                        "classes/META-INF/persistence.xml")
                .addAsWebInfResource("test-default-orm.xml", "classes/META-INF/orm.xml")
                .addAsWebInfResource("test-custom-orm.xml", "classes/META-INF/custom-orm.xml");
    }

    @Test
    public void should_recognize_entity_data()
    {
        // given

        // when
        boolean positive1 = PersistenceUnits.instance().isEntity(MappedOne.class);
        boolean positive2 = PersistenceUnits.instance().isEntity(MappedTwo.class);
        boolean positive3 = PersistenceUnits.instance().isEntity(MappedThree.class);
        boolean negative = PersistenceUnits.instance().isEntity(Long.class);

        // then
        assertTrue(positive1);
        assertTrue(positive2);
        assertTrue(positive3);
        assertFalse(negative);
    }

    @Test
    public void should_recognize_ids()
    {
        // given

        // when
        String idField1 = PersistenceUnits.instance().primaryKeyField(MappedOne.class);
        String idField2 = PersistenceUnits.instance().primaryKeyField(MappedThree.class);

        // then
        assertEquals("id", idField1);
        assertEquals("id", idField2);
    }

    @Test
    public void should_recognize_name()
    {
        // given

        // when
        String name = PersistenceUnits.instance().entityName(MappedOne.class);

        // then
        assertEquals("Mapped_One", name);
    }

    @Test
    public void should_recognize_id_class()
    {
        // given

        // when
        Class<?> idClass = PersistenceUnits.instance().primaryKeyIdClass(MappedTwo.class);

        // then
        assertEquals(TeeId.class, idClass);
    }

    @Test
    public void should_prepare_repo_entity()
    {
        // given

        // when
        RepositoryEntity entity1 = PersistenceUnits.instance().lookupMetadata(MappedOne.class);
        RepositoryEntity entity2 = PersistenceUnits.instance().lookupMetadata(MappedTwo.class);
        RepositoryEntity entity3 = PersistenceUnits.instance().lookupMetadata(MappedThree.class);

        // then
        assertNotNull(entity1);
        assertNotNull(entity2);
        assertEquals(Long.class, entity1.getPrimaryClass());
        assertEquals(TeeId.class, entity2.getPrimaryClass());
        assertEquals(Long.class, entity3.getPrimaryClass());
    }

}
