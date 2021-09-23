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
package org.apache.deltaspike.servlet.impl.config;

import org.apache.deltaspike.core.impl.config.BaseConfigSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This is an _optional_ ConfigSource!
 * It will only provide information if running in a Servlet container!
 */
public class ServletConfigSource extends BaseConfigSource
{
    private final ConcurrentMap<String, String> servletProperties;

    public ServletConfigSource()
    {
        servletProperties = new ConcurrentHashMap<String, String>();
        initOrdinal(50);
    }



    public void setPropertyValue(String key, String value)
    {
        servletProperties.put(key, value);
    }

    @Override
    public Map<String, String> getProperties()
    {
        return servletProperties;
    }

    @Override
    public String getPropertyValue(String key)
    {
        return servletProperties.get(key);
    }

    @Override
    public String getConfigName()
    {
        return "servletconfig-properties";
    }

    @Override
    public boolean isScannable()
    {
        return true;
    }

}
