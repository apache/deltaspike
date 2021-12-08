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

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.spi.config.ConfigSource;

/**
 * JMX MBean for DeltaSpike
 */
public class DeltaSpikeConfigInfo implements DeltaSpikeConfigInfoMBean
{
    private final ClassLoader appConfigClassLoader;

    public DeltaSpikeConfigInfo(ClassLoader appConfigClassLoader)
    {
        this.appConfigClassLoader = appConfigClassLoader;
    }

    @Override
    public String[] getConfigSourcesAsString()
    {
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(appConfigClassLoader);

            ConfigSource[] configSources = ConfigResolver.getConfigSources();
            List<String> configSourceInfo = new ArrayList<String>();
            for (ConfigSource configSource : configSources)
            {
                configSourceInfo.add(Integer.toString(configSource.getOrdinal())
                    + " - " + configSource.getConfigName());
            }

            return configSourceInfo.toArray(new String[configSourceInfo.size()]);
        }
        finally
        {
            // set back the original TCCL
            Thread.currentThread().setContextClassLoader(originalCl);
        }

    }

    @Override
    public String[] getConfigEntriesAsString()
    {
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(appConfigClassLoader);

            List<ConfigEntry> configEntries = calculateConfigEntries();

            String[] configArray = new String[configEntries.size()];

            for (int i = 0 ; i < configEntries.size(); i++)
            {
                ConfigEntry configEntry = configEntries.get(i);
                configArray[i] = configEntry.getKey() + " = " + configEntry.getValue()
                    + " - picked up from: " + configEntry.getFromConfigSource();
            }

            return configArray;

        }
        finally
        {
            // set back the original TCCL
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }

    @Override
    public TabularData getConfigEntries()
    {
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(appConfigClassLoader);

            List<ConfigEntry> configEntries = calculateConfigEntries();

            String[] configArray = new String[configEntries.size()];

            for (int i = 0 ; i < configEntries.size(); i++)
            {
                ConfigEntry configEntry = configEntries.get(i);
                configArray[i] = configEntry.getKey() + " = " + configEntry.getValue()
                    + " - picked up from: " + configEntry.getFromConfigSource();
            }

            String typeName = "ConfigEntries";
            OpenType<?>[] types = new OpenType<?>[]{SimpleType.STRING, SimpleType.STRING, SimpleType.STRING};
            String[] keys = new String[]{"Key", "Value", "fromConfigSource"};

            CompositeType ct = new CompositeType(typeName, typeName, keys, keys, types);
            TabularType type = new TabularType(typeName, typeName, ct, keys);
            TabularDataSupport configEntryInfo = new TabularDataSupport(type);

            ConfigSource[] configSources = ConfigResolver.getConfigSources();
            for (ConfigEntry configEntry : configEntries)
            {
                configEntryInfo.put(
                    new CompositeDataSupport(ct, keys,
                        new Object[]{configEntry.getKey(), configEntry.getValue(), configEntry.getFromConfigSource()}));
            }

            return configEntryInfo;
        }
        catch (OpenDataException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            // set back the original TCCL
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }

    @Override
    public TabularData getConfigSources()
    {
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(appConfigClassLoader);

            String typeName = "ConfigSources";
            OpenType<?>[] types = new OpenType<?>[]{SimpleType.INTEGER, SimpleType.STRING};
            String[] keys = new String[]{"Ordinal", "ConfigSource"};

            CompositeType ct = new CompositeType(typeName, typeName, keys, keys, types);
            TabularType type = new TabularType(typeName, typeName, ct, keys);
            TabularDataSupport configSourceInfo = new TabularDataSupport(type);

            ConfigSource[] configSources = ConfigResolver.getConfigSources();
            for (ConfigSource configSource : configSources)
            {
                configSourceInfo.put(
                    new CompositeDataSupport(ct, keys,
                            new Object[]{configSource.getOrdinal(), configSource.getConfigName()}));
            }

            return configSourceInfo;
        }
        catch (OpenDataException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            // set back the original TCCL
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }

    private List<ConfigEntry> calculateConfigEntries()
    {
        Map<String, String> allProperties = ConfigResolver.getAllProperties();
        List<ConfigEntry> configEntries = new ArrayList<ConfigEntry>(allProperties.size());
        ConfigSource[] configSources = ConfigResolver.getConfigSources();

        for (Map.Entry<String, String> configEntry : allProperties.entrySet())
        {
            String key = configEntry.getKey();
            String value = ConfigResolver.filterConfigValueForLog(key,
                                    ConfigResolver.getProjectStageAwarePropertyValue(key));

            String fromConfigSource = getFromConfigSource(configSources, key);
            configEntries.add(new ConfigEntry(key, value, fromConfigSource));
        }

        return configEntries;
    }

    private String getFromConfigSource(ConfigSource[] configSources, String key)
    {
        for (ConfigSource configSource : configSources)
        {
            if (configSource.getPropertyValue(key) != null)
            {
                return configSource.getConfigName();
            }
        }

        return null;
    }



    private class ConfigEntry
    {
        private final String key;
        private final String value;
        private final String fromConfigSource;

        ConfigEntry(String key, String value, String fromConfigSource)
        {
            this.key = key;
            this.value = value;
            this.fromConfigSource = fromConfigSource;
        }

        String getKey()
        {
            return key;
        }

        String getValue()
        {
            return value;
        }

        String getFromConfigSource()
        {
            return fromConfigSource;
        }
    }

}
