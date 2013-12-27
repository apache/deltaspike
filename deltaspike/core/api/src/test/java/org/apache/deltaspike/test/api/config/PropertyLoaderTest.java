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
package org.apache.deltaspike.test.api.config;

import org.apache.deltaspike.core.api.config.PropertyLoader;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class PropertyLoaderTest
{
    @Before
    public void init()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.UnitTest);
    }


    @Test
    public void testNotExistingPropertyLoading() throws Exception
    {
        Assert.assertNull(PropertyLoader.getProperties("notexistingProperty"));
        Assert.assertNull(PropertyLoader.getProperties("notexistingProperty.properties"));
    }

    @Test
    public void testStandardPropertyLoading() throws Exception
    {
        checkProperties("test", "1");
        checkProperties("test.properties", "1");
    }

    @Test
    public void testProjectStagedPropertyLoading() throws Exception
    {
        checkProperties("test2", "2");
        checkProperties("test2.properties", "2");
    }


    private void checkProperties(String propertyFile, String expectedValue)
    {
        Properties p = PropertyLoader.getProperties(propertyFile);
        Assert.assertNotNull(p);
        Assert.assertEquals(1, p.size());
        Assert.assertEquals(expectedValue, p.get("test.value"));
    }

}
