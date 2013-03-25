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
package org.apache.deltaspike.core.impl.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Base class for configuration sources based on {@link Properties} object.
 */
public abstract class PropertiesConfigSource extends BaseConfigSource
{

    private final Properties properties;

    protected PropertiesConfigSource(Properties properties)
    {
        this.properties = properties;
    }

    /**
     * The given key gets used for a lookup via a properties object
     *
     * @param key for the property
     * @return value for the given key or null if there is no configured value
     */
    @Override
    public String getPropertyValue(String key)
    {
        return properties.getProperty(key);
    }

    @Override
    public Map<String, String> getProperties()
    {
        Map<String,String> result = new HashMap<String, String>();
        for (String propertyName : properties.stringPropertyNames())
        {
            result.put(propertyName, properties.getProperty(propertyName));
        }

        return result;
    }

    @Override
    public boolean isScannable()
    {
        return true;
    }
}