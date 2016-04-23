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
package org.apache.deltaspike.testcontrol.api.junit;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.base.DeltaSpikeBaseConfig;

public interface TestBaseConfig extends DeltaSpikeBaseConfig
{
    interface ContainerIntegration
    {
        //default is false to improve the compatibility with @Before and @After
        Boolean USE_TEST_CLASS_AS_CDI_BEAN = ConfigResolver.resolve("deltaspike.testcontrol.use_test_class_as_cdi_bean")
                .as(Boolean.class)
                .withCurrentProjectStage(true)
                .withDefault(Boolean.FALSE)
                .getValue();

        Boolean STOP_CONTAINER = ConfigResolver.resolve("deltaspike.testcontrol.stop_container")
                .as(Boolean.class)
                .withCurrentProjectStage(true)
                .withDefault(Boolean.TRUE)
                .getValue();
    }

    interface MockIntegration
    {
        String ALLOW_MOCKED_BEANS_KEY = "deltaspike.testcontrol.mock-support.allow_mocked_beans";
        String ALLOW_MOCKED_PRODUCERS_KEY = "deltaspike.testcontrol.mock-support.allow_mocked_producers";
        String ALLOW_MANUAL_INJECTION_POINT_MANIPULATION_KEY =
            "deltaspike.testcontrol.mock-support.allow_manual_injection-point_manipulation";

        Boolean ALLOW_MOCKED_BEANS = ConfigResolver.resolve(ALLOW_MOCKED_BEANS_KEY)
                .as(Boolean.class)
                .withCurrentProjectStage(true)
                .withDefault(Boolean.FALSE)
                .getValue();

        Boolean ALLOW_MOCKED_PRODUCERS = ConfigResolver.resolve(ALLOW_MOCKED_PRODUCERS_KEY)
                .as(Boolean.class)
                .withCurrentProjectStage(true)
                .withDefault(Boolean.FALSE)
                .getValue();

        //if enabled it's possible to change the value of injection-points after the injection-process and
        //before test-execution. that allows to replace injection-points (e.g. with a mock) conditionally
        //via a test-rule or @Before
        Boolean ALLOW_MANUAL_INJECTION_POINT_MANIPULATION =
            ConfigResolver.resolve(ALLOW_MANUAL_INJECTION_POINT_MANIPULATION_KEY)
                .as(Boolean.class)
                .withCurrentProjectStage(true)
                .withDefault(Boolean.FALSE)
                .getValue();
    }
}
