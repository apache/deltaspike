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
package org.apache.deltaspike.test.core.api.projectstage;


import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.api.projectstage.ProjectStageHolder;

/**
 * This is a test ProjectStage. It demonstrates how to add custom ProjectStages.
 * This TestProjectStage must get registered via the {@link java.util.ServiceLoader}
 * mechanism. Please see the file
 * <pre>
 *     META-INF/services/org.apache.deltaspike.core.api.projectstage.ProjectStageHolder
 * </pre>
 */
public class TestProjectStages implements ProjectStageHolder
{
    public static final class CustomProjectStage extends ProjectStage
    {
        private static final long serialVersionUID = 1029095387976167179L;
    }

    public static final CustomProjectStage CustomProjectStage = new CustomProjectStage();
}
