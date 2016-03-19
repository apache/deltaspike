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
import static org.junit.Assert.assertNull;

import java.util.LinkedList;
import java.util.List;
import org.apache.deltaspike.jpa.spi.descriptor.xml.AbstractEntityHierarchyBuilder;
import org.apache.deltaspike.jpa.spi.descriptor.xml.EntityDescriptor;
import org.apache.deltaspike.jpa.spi.descriptor.xml.MappedSuperclassDescriptor;

import org.junit.Before;
import org.junit.Test;

public class DescriptorHierarchyBuilderTest
{

    private final List<EntityDescriptor> entities = new LinkedList<EntityDescriptor>();
    private final List<MappedSuperclassDescriptor> superClasses = new LinkedList<MappedSuperclassDescriptor>();

    @Before
    public void before()
    {
        entities.add(new EntityDescriptor(null, null, "test", EntityLevel3.class, null, null, null));
        entities.add(new EntityDescriptor(null, null, "test", EntityLevel5.class, null, null, null));

        superClasses.add(new MappedSuperclassDescriptor(new String[] { "id" }, null, "test", MappedLevel1.class,
            null, null));
        superClasses.add(new MappedSuperclassDescriptor(null, null, "test", MappedLevel4.class, null, null));
        superClasses.add(new MappedSuperclassDescriptor(null, null, "test", MappedUnrelated.class, null, null));
        superClasses.add(new MappedSuperclassDescriptor(null, null, "test", MappedLevel2.class, null, null));
    }

    @Test
    public void should_build_hierarchy()
    {
        // when
        AbstractEntityHierarchyBuilder.buildHierarchy(entities, superClasses);

        // then
        assertEquals(entities.size(), 2);
        assertEquals(EntityLevel3.class, entities.get(0).getEntityClass());
        assertEquals(EntityLevel5.class, entities.get(1).getEntityClass());
        assertEquals(MappedLevel2.class, entities.get(0).getParent().getEntityClass());
        assertEquals(MappedLevel4.class, entities.get(1).getParent().getEntityClass());

        assertEquals(superClasses.size(), 4);
        assertNull(superClasses.get(0).getParent());
        assertEquals(EntityLevel3.class, superClasses.get(1).getParent().getEntityClass());
        assertNull(superClasses.get(2).getParent());
        assertEquals(MappedLevel1.class, superClasses.get(3).getParent().getEntityClass());
    }

    private static class MappedLevel1
    {
        @SuppressWarnings("unused")
        private Long id;
    }

    private static class MappedLevel2 extends MappedLevel1
    {
    }

    private static class EntityLevel3 extends MappedLevel2
    {
    }

    private static class MappedLevel4 extends EntityLevel3
    {
    }

    private static class EntityLevel5 extends MappedLevel4
    {
    }

    private static class MappedUnrelated
    {
    }
}
