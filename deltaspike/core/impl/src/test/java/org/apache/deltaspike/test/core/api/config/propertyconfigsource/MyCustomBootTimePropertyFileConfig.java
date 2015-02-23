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
package org.apache.deltaspike.test.core.api.config.propertyconfigsource;

import org.apache.deltaspike.core.api.config.PropertyFileConfig;
import org.apache.deltaspike.core.api.exclude.Exclude;

/**
 * Custom PropertyFileConfig which gets picked up via {@code java.util.ServiceLoader}
 * The values will already be available <em>before</em> the container gets booted!
 */
@Exclude // this is important to let the ConfigurationExtension know that it should not handle it
public class MyCustomBootTimePropertyFileConfig implements PropertyFileConfig
{
    @Override
    public String getPropertyFileName()
    {
        return "myboottimeconfig.properties";
    }

    @Override
    public boolean isOptional()
    {
        return false;
    }
}
