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

import org.apache.deltaspike.core.api.config.base.TypedConfig;

public interface TestBaseConfig
{
    interface Container
    {
        //default is false to improve the compatibility with @Before and @After
        TypedConfig<Boolean> USE_TEST_CLASS_AS_CDI_BEAN =
            new TypedConfig<Boolean>("deltaspike.testcontrol.use_test_class_as_cdi_bean", Boolean.FALSE);

        TypedConfig<Boolean> STOP_CONTAINER =
            new TypedConfig<Boolean>("deltaspike.testcontrol.stop_container", Boolean.TRUE);
    }

    interface Mock
    {
        TypedConfig<Boolean> ALLOW_MOCKED_BEANS =
                new TypedConfig<Boolean>("deltaspike.testcontrol.mock-support.allow_mocked_beans", Boolean.FALSE);

        TypedConfig<Boolean> ALLOW_MOCKED_PRODUCERS =
                new TypedConfig<Boolean>("deltaspike.testcontrol.mock-support.allow_mocked_producers", Boolean.FALSE);

    }
}
