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
package org.apache.deltaspike.test.util.activation;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.junit.Assert;
import org.junit.Test;

public class ProjectStageDependentClassDeactivationTest
{
    @Test
    public void deactivationResultInProjectStageUnitTest()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.UnitTest);

        final Class<? extends Deactivatable> classToCheck = TestDeactivatable.class;
        EditableTestDeactivator.activate(classToCheck);
        Assert.assertEquals(true, ClassDeactivationUtils.isActivated(classToCheck));

        EditableTestDeactivator.deactivate(classToCheck);
        Assert.assertEquals(false, ClassDeactivationUtils.isActivated(classToCheck));
    }

    @Test
    public void deactivationResultInProjectStageDevelopment()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.Development);

        final Class<? extends Deactivatable> classToCheck = TestDeactivatable.class;
        EditableTestDeactivator.activate(classToCheck);
        Assert.assertEquals(true, ClassDeactivationUtils.isActivated(classToCheck));

        EditableTestDeactivator.deactivate(classToCheck);
        Assert.assertEquals(false, ClassDeactivationUtils.isActivated(classToCheck));
    }

    @Test
    public void deactivationResultInProjectStageProduction()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.Production);

        final Class<? extends Deactivatable> classToCheck = TestDeactivatable.class;
        EditableTestDeactivator.activate(classToCheck);
        Assert.assertEquals(true, ClassDeactivationUtils.isActivated(classToCheck));

        EditableTestDeactivator.deactivate(classToCheck);
        Assert.assertEquals(true, ClassDeactivationUtils.isActivated(classToCheck)); //due to the cached result
    }

    @Test
    public void deactivationResultInProjectStageIntegrationTest()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.IntegrationTest);

        final Class<? extends Deactivatable> classToCheck = TestDeactivatable.class;
        EditableTestDeactivator.activate(classToCheck);
        Assert.assertEquals(true, ClassDeactivationUtils.isActivated(classToCheck));

        EditableTestDeactivator.deactivate(classToCheck);
        Assert.assertEquals(true, ClassDeactivationUtils.isActivated(classToCheck)); //due to the cached result
    }

    @Test
    public void deactivationResultInProjectStageStaging()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.Staging);

        final Class<? extends Deactivatable> classToCheck = TestDeactivatable.class;
        EditableTestDeactivator.activate(classToCheck);
        Assert.assertEquals(true, ClassDeactivationUtils.isActivated(classToCheck));

        EditableTestDeactivator.deactivate(classToCheck);
        Assert.assertEquals(true, ClassDeactivationUtils.isActivated(classToCheck)); //due to the cached result
    }

    @Test
    public void deactivationResultInProjectStageSystemTest()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.SystemTest);

        final Class<? extends Deactivatable> classToCheck = TestDeactivatable.class;
        EditableTestDeactivator.activate(classToCheck);
        Assert.assertEquals(true, ClassDeactivationUtils.isActivated(classToCheck));

        EditableTestDeactivator.deactivate(classToCheck);
        Assert.assertEquals(true, ClassDeactivationUtils.isActivated(classToCheck)); //due to the cached result
    }

    private static class TestDeactivatable implements Deactivatable
    {
    }
}
