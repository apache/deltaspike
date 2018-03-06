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
package org.apache.deltaspike.test.testcontrol.uc013;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestSuiteRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


//Usually NOT needed! Currently only needed due to our arquillian-setup
@Category(SeCategory.class)



@RunWith(CdiTestRunner.class)
public class ContainerConfigTest
{

    @Test
    @TestControl(projectStage = ProjectStage.UnitTest.class) //just for internal tests
    public void configForTestContainerStageUnitTest()
    {
        Assert.assertNotNull(CdiTestSuiteRunner.getTestContainerConfig());
        Assert.assertEquals("jdbc:hsqldb:mem:demoDB",
            CdiTestSuiteRunner.getTestContainerConfig().getProperty("demoDatabase.JdbcUrl"));
    }

    @Test
    @TestControl(projectStage = ProjectStage.IntegrationTest.class) //just for internal tests
    public void configForTestContainerStageIntegrationTest()
    {
        Assert.assertNotNull(CdiTestSuiteRunner.getTestContainerConfig());
        Assert.assertEquals("jdbc:hsqldb:file:demoDB",
            CdiTestSuiteRunner.getTestContainerConfig().getProperty("demoDatabase.JdbcUrl"));
    }
}
