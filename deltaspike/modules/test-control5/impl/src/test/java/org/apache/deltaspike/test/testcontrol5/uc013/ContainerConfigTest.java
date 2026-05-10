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
package org.apache.deltaspike.test.testcontrol5.uc013;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.testcontrol5.api.TestControl;
import org.apache.deltaspike.testcontrol5.api.junit.CdiTestExtension;
import org.apache.deltaspike.testcontrol5.api.junit.CdiTestSuiteExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

//Usually NOT needed! Currently only needed due to our arquillian-setup
@Tag("SeCategory")

@ExtendWith(CdiTestExtension.class)
public class ContainerConfigTest
{

    @Test
    @TestControl(projectStage = ProjectStage.UnitTest.class) //just for internal tests
    public void configForTestContainerStageUnitTest()
    {
        assertNotNull(CdiTestSuiteExtension.getTestContainerConfig());
        assertEquals("jdbc:hsqldb:mem:demoDB",
            CdiTestSuiteExtension.getTestContainerConfig().getProperty("demoDatabase.JdbcUrl"));
    }

    @Test
    @TestControl(projectStage = ProjectStage.IntegrationTest.class) //just for internal tests
    public void configForTestContainerStageIntegrationTest()
    {
        assertNotNull(CdiTestSuiteExtension.getTestContainerConfig());
        assertEquals("jdbc:hsqldb:file:demoDB",
            CdiTestSuiteExtension.getTestContainerConfig().getProperty("demoDatabase.JdbcUrl"));
    }
}
