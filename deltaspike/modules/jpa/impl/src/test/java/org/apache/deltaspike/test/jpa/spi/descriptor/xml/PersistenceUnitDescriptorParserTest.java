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
import java.util.List;
import org.apache.deltaspike.jpa.spi.descriptor.xml.PersistenceUnitDescriptor;
import org.apache.deltaspike.jpa.spi.descriptor.xml.PersistenceUnitDescriptorParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersistenceUnitDescriptorParserTest
{
    private PersistenceUnitDescriptorParser persistenceUnitDescriptorParser;
    private List<PersistenceUnitDescriptor> descriptors;

    @Before
    public void before() throws IOException
    {
        persistenceUnitDescriptorParser
            = new PersistenceUnitDescriptorParser();
        descriptors
            = persistenceUnitDescriptorParser.readAll();
    }
    
    @Test
    public void testPackageName() throws IOException
    {
        Assert.assertEquals("test", descriptors.get(0).getName());
        Assert.assertEquals("test2", descriptors.get(1).getName());
    }
    
    @Test
    public void testProperties() throws IOException
    {
        Assert.assertNotNull(descriptors.get(0).getProperties());
        Assert.assertEquals(1, descriptors.get(0).getProperties().size());
        
        Assert.assertNotNull(descriptors.get(1).getProperties());
        Assert.assertEquals(3, descriptors.get(1).getProperties().size());
        
        Assert.assertNotNull(descriptors.get(2).getProperties());
        Assert.assertEquals(0, descriptors.get(2).getProperties().size());
    }
    
    @Test
    public void testEntityDescriptors() throws IOException
    {
        Assert.assertNotNull(descriptors.get(0).getEntityDescriptors());
        Assert.assertEquals(3, descriptors.get(0).getEntityDescriptors().size());
        Assert.assertEquals(MappedOne.class,
            descriptors.get(0).getEntityDescriptors().get(0).getEntityClass());
    }
}
