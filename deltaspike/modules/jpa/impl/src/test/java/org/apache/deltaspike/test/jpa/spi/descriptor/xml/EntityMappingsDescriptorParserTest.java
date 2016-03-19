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
package org.apache.deltaspike.test.jpa.spi.descriptor.xml;

import java.io.IOException;
import org.apache.deltaspike.jpa.spi.descriptor.xml.EntityMappingsDescriptor;
import org.apache.deltaspike.jpa.spi.descriptor.xml.EntityMappingsDescriptorParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EntityMappingsDescriptorParserTest
{
    private EntityMappingsDescriptorParser entityMappingsDescriptorParser;
    private EntityMappingsDescriptor descriptor;

    @Before
    public void before() throws IOException
    {
        entityMappingsDescriptorParser
            = new EntityMappingsDescriptorParser();
        descriptor
            = entityMappingsDescriptorParser.readAll(getClass().getResource("/").getPath(), "META-INF/test-orm.xml");
    }
    
    @Test
    public void testPackageName() throws IOException
    {
        Assert.assertEquals("org.apache.deltaspike.test.jpa.spi.descriptor.xml",
                descriptor.getPackageName());
    }
    
    @Test
    public void testEntityDescriptors() throws IOException
    {
        Assert.assertNotNull(descriptor.getEntityDescriptors());
        Assert.assertEquals(3, descriptor.getEntityDescriptors().size());
    }
    
    @Test
    public void testMappedSuperclassDescriptors() throws IOException
    {
        Assert.assertNotNull(descriptor.getMappedSuperclassDescriptors());
        Assert.assertEquals(2, descriptor.getMappedSuperclassDescriptors().size());
    }
    
    @Test
    public void testEntityClass() throws IOException
    {
        Assert.assertEquals(MappedOne.class,
                descriptor.getEntityDescriptors().get(0).getEntityClass());
        Assert.assertEquals(MappedTwo.class,
                descriptor.getEntityDescriptors().get(1).getEntityClass());
        Assert.assertEquals(MappedThree.class,
                descriptor.getEntityDescriptors().get(2).getEntityClass());
    }
    
    @Test
    public void testId() throws IOException
    {
        Assert.assertEquals("id",
                descriptor.getEntityDescriptors().get(0).getId()[0]);
        Assert.assertEquals("teeSetId",
                descriptor.getEntityDescriptors().get(1).getId()[0]);
        Assert.assertEquals("holeId",
                descriptor.getEntityDescriptors().get(1).getId()[1]);
        Assert.assertEquals(null,
                descriptor.getEntityDescriptors().get(2).getId());
    }
    
    @Test
    public void testVersion() throws IOException
    {
        Assert.assertEquals("version",
                descriptor.getEntityDescriptors().get(0).getVersion());
        Assert.assertEquals(null,
                descriptor.getEntityDescriptors().get(1).getVersion());
        Assert.assertEquals(null,
                descriptor.getEntityDescriptors().get(2).getVersion());
        
        Assert.assertEquals(null, descriptor.getMappedSuperclassDescriptors().get(0).getVersion());
        Assert.assertEquals("version", descriptor.getMappedSuperclassDescriptors().get(1).getVersion());
    }
    
    @Test
    public void testName() throws IOException
    {
        Assert.assertEquals("Mapped_One",
                descriptor.getEntityDescriptors().get(0).getName());
    }
    
    @Test
    public void testTableName() throws IOException
    {
        Assert.assertEquals("mapped_three_table",
                descriptor.getEntityDescriptors().get(2).getTableName());
    }
}
