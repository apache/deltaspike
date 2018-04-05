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
package org.apache.deltaspike.test.core.api.config;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.spi.config.ConfigSource;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A ConfigSource which is backed by a ThreadLocal.
 * So it can be dynamically configured even for parallel tests.
 *
 * Note that you MUST call the {@link #clear()} method at the end of a method which uses this ConfigSource.
 */
public class ConfigurableTestConfigSource implements ConfigSource
{
    private static ThreadLocal<Map<String, String>> props = new ThreadLocal<>();

    private Consumer<Set<String>> reportAttributeChange;

    @Override
    public int getOrdinal()
    {
        return 500;
    }

    public static ConfigurableTestConfigSource instance() {
        return (ConfigurableTestConfigSource) Arrays.stream(ConfigResolver.getConfig().getConfigSources())
                .filter(cs -> cs instanceof ConfigurableTestConfigSource)
                .findFirst()
                .get();
    }

    @Override
    public Map<String, String> getProperties()
    {
        Map<String, String> propMap = props.get();
        if (propMap == null)
        {
            propMap = new ConcurrentHashMap<>();
            props.set(propMap);
        }
        return propMap;
    }

    @Override
    public String getPropertyValue(String key)
    {
        return getProperties().get(key);
    }

    @Override
    public String getConfigName()
    {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean isScannable()
    {
        return true;
    }

    public void clear()
    {
        props.set(null);
        props.remove();
    }

    public void setValues(Map<String, String> values)
    {
        getProperties().putAll(values);

        // now notify our Config that some values got changed
        reportAttributeChange.accept(values.keySet());
    }

    @Override
    public void setOnAttributeChange(Consumer<Set<String>> reportAttributeChange)
    {
        this.reportAttributeChange = reportAttributeChange;
    }
}
