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
package org.apache.deltaspike.testcontrol5.api.junit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.spi.config.ConfigSource;

//config-sources are already stored per classloader
//keep it public to allow type-safe deactivation (if needed)
public class TestConfigSource implements ConfigSource, Deactivatable
{
    private Map<String, String> testConfig = new ConcurrentHashMap<String, String>();

    @Override
    public int getOrdinal()
    {
        return Integer.MIN_VALUE;
    }

    @Override
    public Map<String, String> getProperties()
    {
        return testConfig;
    }

    @Override
    public String getPropertyValue(String key)
    {
        return testConfig.get(key);
    }

    @Override
    public String getConfigName()
    {
        return "ds-test-config";
    }

    @Override
    public boolean isScannable()
    {
        return true;
    }
}
