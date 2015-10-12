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
package org.apache.deltaspike.core.util;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.activation.BaseClassDeactivationController;
import org.apache.deltaspike.core.util.activation.CachingClassDeactivationController;
import org.apache.deltaspike.core.util.activation.NonCachingClassDeactivationController;

import javax.enterprise.inject.Typed;
import java.util.Arrays;
import java.util.List;

/**
 * Helper methods for {@link ClassDeactivator}
 */
@Typed()
public abstract class ClassDeactivationUtils
{
    private static final List<ProjectStage> NON_CACHING_PROJECT_STAGES =
        Arrays.asList(ProjectStage.Development,ProjectStage.UnitTest);

    private static BaseClassDeactivationController classDeactivationController = null;

    private ClassDeactivationUtils()
    {
        // prevent instantiation
    }

    /**
     * Evaluates if the given {@link Deactivatable} is active.
     *
     * @param targetClass {@link Deactivatable} under test.
     * @return <code>true</code> if it is active, <code>false</code> otherwise
     */
    public static boolean isActivated(Class<? extends Deactivatable> targetClass)
    {
        return getController().isActivated(targetClass);
    }

    private static BaseClassDeactivationController getController()
    {
        if (classDeactivationController == null)
        {
            classDeactivationController = calculateControllerToUse();
        }

        return classDeactivationController;
    }

    private static BaseClassDeactivationController calculateControllerToUse()
    {
        ProjectStage currentProjectStage = ProjectStageProducer.getInstance().getProjectStage();

        if (NON_CACHING_PROJECT_STAGES.contains(currentProjectStage))
        {
            return new NonCachingClassDeactivationController();
        }
        else
        {
            return new CachingClassDeactivationController();
        }
    }
}
