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
package org.apache.deltaspike.jpa.api.config.base;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.base.DeltaSpikeBaseConfig;

public interface JpaBaseConfig extends DeltaSpikeBaseConfig
{
    interface UserTransaction
    {
        String JNDI_NAME = ConfigResolver.resolve("deltaspike.jpa.user-transaction.jndi-name")
                .withCurrentProjectStage(true)
                .withDefault("java:comp/UserTransaction")
                .getValue();

        Integer TIMEOUT_IN_SECONDS = ConfigResolver.resolve("deltaspike.jpa.user-transaction.timeout_in_seconds")
                .as(Integer.class)
                .withCurrentProjectStage(true)
                .getValue();
    }
}
