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
package org.apache.deltaspike.test.testcontrol.uc004;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.testcontrol.uc001.RequestAndSessionScopePerTestMethodTest;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

//Usually NOT needed! Currently only needed due to our arquillian-setup
@Category(SeCategory.class)



@RunWith(CdiTestRunner.class)
@TestControl(projectStage = ProjectStage.Development.class) //can be a custom stage
@Typed() //needed due to RequestAndSessionScopePerTestMethodTest
public class ProjectStageTestControlTest extends RequestAndSessionScopePerTestMethodTest //just to inherit the tests - to check that @TestControl#projectStage doesn't influence the default handling
{
    @Inject
    private ProjectStage projectStage;

    @Test
    public void firstProjectStageTest()
    {
        Assert.assertEquals(ProjectStage.Development, this.projectStage);
    }

    @Test
    @TestControl(projectStage = ProjectStage.UnitTest.class) //can be a custom stage - e.g. for a special test
    public void secondProjectStageTest()
    {
        Assert.assertEquals(ProjectStage.UnitTest, this.projectStage);
    }
}
