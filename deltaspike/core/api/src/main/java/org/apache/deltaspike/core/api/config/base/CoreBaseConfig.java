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
package org.apache.deltaspike.core.api.config.base;

import org.apache.deltaspike.core.api.config.ConfigResolver;

public interface CoreBaseConfig
{
    interface BeanManagerDelegation
    {
        Boolean DELEGATE_LOOKUP =
                ConfigResolver.resolve("deltaspike.bean-manager.delegate_lookup")
                        .as(Boolean.class)
                        .withCurrentProjectStage(true)
                        .withDefault(Boolean.TRUE)
                        .getValue();
    }

    interface Interceptor
    {
        Integer PRIORITY =
                ConfigResolver.resolve("deltaspike.interceptor.priority")
                        .as(Integer.class)
                        .withCurrentProjectStage(true)
                        .withDefault(0)
                        .getValue();
    }

    interface MBean
    {
        Boolean AUTO_UNREGISTER =
                ConfigResolver.resolve("deltaspike.mbean.auto-unregister")
                        .as(Boolean.class)
                        .withCurrentProjectStage(true)
                        .withDefault(Boolean.TRUE)
                        .getValue();
    }

    interface Scope
    {
        interface WindowRestriction
        {
            String MAX_COUNT_KEY = "deltaspike.scope.window.max-count";

            Integer MAX_COUNT =
                    ConfigResolver.resolve(MAX_COUNT_KEY)
                            .as(Integer.class)
                            .withCurrentProjectStage(true)
                            .withDefault(1024)
                            .getValue();

        }
    }
}
