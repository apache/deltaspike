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
package org.apache.deltaspike.core.api.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;

import javax.enterprise.inject.Typed;

/**
 * Resolve the configuration via their well defined ordinals.
 */
@Typed()
public final class ConfigResolver
{
    private static final Logger LOG = Logger.getLogger(ConfigResolver.class.getName());

    /**
     * The content of this map will get lazily initiated and will hold the
     * sorted List of ConfigSources for each WebApp/EAR, etc (thus the
     * ClassLoader).
     */
    private static Map<ClassLoader, ConfigSource[]> configSources
        = new ConcurrentHashMap<ClassLoader, ConfigSource[]>();

    private ConfigResolver()
    {
        // this is a utility class which doesn't get instantiated.
    }

    /**
     * Resolve the property value by going through the list of configured {@link ConfigSource}s
     * and use the one with the highest priority.
     *
     * @param key the property key.
     * @return the configured property value from the {@link ConfigSource} with the highest ordinal or
     * null if there is no configured value for it.
     */
    public static String getPropertyValue(String key)
    {
        ConfigSource[] appConfigSources = getConfigSources();

        for (ConfigSource cs : appConfigSources)
        {
            String val = cs.getPropertyValue(key);
            if (val != null)
            {
                LOG.log(Level.FINE, "found value {0} for key {1} in ConfigSource {2}.",
                        new Object[]{val, key, cs.getConfigName()});
                return val;
            }

            LOG.log(Level.FINER, "NO value found for key {0} in ConfigSource {1}.",
                    new Object[]{key, cs.getConfigName()});
        }

        return null;
    }

    /**
     * Resolve all values for the given key, from all registered ConfigSources ordered by their
     * ordinal value in ascending ways. If more {@link ConfigSource}s have the same ordinal, their
     * order is undefined.
     *
     * @param key under which configuration is stored
     * @return List with all found property values, sorted in ascending order of their ordinal.
     * @see org.apache.deltaspike.core.spi.config.ConfigSource#getOrdinal()
     */
    public static List<String> getAllPropertyValues(String key)
    {
        List<ConfigSource> appConfigSources = sortAscending(Arrays.asList(getConfigSources()));
        List<String> allPropValues = new ArrayList<String>();

        for (ConfigSource cs : appConfigSources)
        {
            String val = cs.getPropertyValue(key);
            if (val != null && !allPropValues.contains(val))
            {
                allPropValues.add(val);
            }
        }

        return allPropValues;
    }

    private static synchronized ConfigSource[] getConfigSources()
    {
        ClassLoader currentCl = ClassUtils.getClassLoader(null);

        ConfigSource[] appConfigSources = configSources.get(currentCl);

        if (appConfigSources == null)
        {
            appConfigSources = sortDescending(resolveConfigSources());

            if (LOG.isLoggable(Level.FINE))
            {
                for (ConfigSource cs : appConfigSources)
                {
                    LOG.log(Level.FINE, "Adding ordinal {0} ConfigSource {1}",
                            new Object[]{cs.getOrdinal(), cs.getConfigName()});
                }
            }

            configSources.put(currentCl, appConfigSources);
        }

        return appConfigSources;
    }

    private static List<ConfigSource> resolveConfigSources()
    {
        List<ConfigSource> appConfigSources = new ArrayList<ConfigSource>();

        ServiceLoader<ConfigSourceProvider> configSourceProviderServiceLoader
            = ServiceLoader.load(ConfigSourceProvider.class);

        for (ConfigSourceProvider csp : configSourceProviderServiceLoader)
        {
            appConfigSources.addAll(csp.getConfigSources());
        }

        return appConfigSources;
    }

    private static ConfigSource[] sortDescending(List<ConfigSource> configSources)
    {
        Collections.sort(configSources, new Comparator<ConfigSource>()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public int compare(ConfigSource configSource1, ConfigSource configSource2)
            {
                return (configSource1.getOrdinal() > configSource2.getOrdinal()) ? -1 : 1;
            }
        });
        return configSources.toArray(new ConfigSource[configSources.size()]);
    }

    private static List<ConfigSource> sortAscending(List<ConfigSource> configSources)
    {
        Collections.sort(configSources, new Comparator<ConfigSource>()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public int compare(ConfigSource configSource1, ConfigSource configSource2)
            {
                return (configSource1.getOrdinal() > configSource2.getOrdinal()) ? 1 : -1;
            }
        });
        return configSources;
    }
}
