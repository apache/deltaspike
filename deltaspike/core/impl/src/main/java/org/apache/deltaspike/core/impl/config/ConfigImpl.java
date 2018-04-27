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

import org.apache.deltaspike.core.api.config.Config;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.ConfigSnapshot;
import org.apache.deltaspike.core.spi.config.ConfigFilter;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;
import org.apache.deltaspike.core.util.ServiceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The internal implementation of the Config interface
 */
public class ConfigImpl implements Config
{
    private static final Logger LOG = Logger.getLogger(ConfigImpl.class.getName());

    private final ClassLoader classLoader;

    private ConfigSource[] configSources;
    private List<ConfigFilter> configFilters;

    // volatile to a.) make the read/write behave atomic and b.) guarantee multi-thread safety
    private volatile long lastChanged = 0;

    public ConfigImpl(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

    /**
     * Performs all the initialisation of the default
     * ConfigSources, ConfigFilters, etc
     */
    void init()
    {
        List<ConfigSource> appConfigSources
            = ServiceUtils.loadServiceImplementations(ConfigSource.class, false, classLoader);

        List<ConfigSourceProvider> configSourceProviderServiceLoader
            = ServiceUtils.loadServiceImplementations(ConfigSourceProvider.class, false, classLoader);

        for (ConfigSourceProvider configSourceProvider : configSourceProviderServiceLoader)
        {
            appConfigSources.addAll(configSourceProvider.getConfigSources());
        }
        addConfigSources(appConfigSources);

        if (LOG.isLoggable(Level.FINE))
        {
            for (ConfigSource cs : appConfigSources)
            {
                LOG.log(Level.FINE, "Adding ordinal {0} ConfigSource {1}",
                        new Object[]{cs.getOrdinal(), cs.getConfigName()});
            }
        }

        List<ConfigFilter> configFilters
            = ServiceUtils.loadServiceImplementations(ConfigFilter.class, false, classLoader);
        this.configFilters = new CopyOnWriteArrayList<>(configFilters);
    }

    /**
     * Shuts down the Config.
     * This will also close all ConfigSources and ConfigFilters which
     * implment the {@link java.lang.AutoCloseable} interface.
     */
    void release()
    {
        for (ConfigSource configSource : configSources)
        {
            close(configSource);
        }

        for (ConfigFilter configFilter : configFilters)
        {
            close(configFilter);
        }
    }

    private void close(Object o)
    {
        if (o instanceof AutoCloseable)
        {
            try
            {
                ((AutoCloseable) o).close();
            }
            catch (Exception e)
            {
                LOG.log(Level.INFO, "Exception while closing " + o.toString(), e);
            }
        }
    }


    @Override
    public ConfigSource[] getConfigSources()
    {
        return configSources;
    }

    @Override
    public ConfigSnapshot snapshotFor(ConfigResolver.TypedResolver<?>... typedResolvers)
    {
        // we implement kind of optimistic Locking
        // Means we try multiple time to resolve all the given values
        // until the config didn't change inbetween.
        for (int tries = 1; tries < 5; tries++)
        {
            Map<ConfigResolver.TypedResolver<?>, Object> configValues = new HashMap<>();
            long startReadLastChanged = lastChanged;
            for (ConfigResolver.TypedResolver<?> typedResolver : typedResolvers)
            {
                configValues.put(typedResolver, typedResolver.getValue());
            }

            if (startReadLastChanged == lastChanged)
            {
                return new ConfigSnapshotImpl(configValues);
            }
        }

        throw new IllegalStateException(
                "Could not resolve ConfigTransaction as underlying values are permanently changing!");
    }

    @Override
    public void addConfigSources(List<ConfigSource> configSourcesToAdd)
    {
        if (configSourcesToAdd == null || configSourcesToAdd.isEmpty())
        {
            return;
        }

        List<ConfigSource> allConfigSources = new ArrayList<>();
        // start with all existing ConfigSources
        if (this.configSources != null)
        {
            for (ConfigSource configSource : this.configSources)
            {
                allConfigSources.add(configSource);
            }
        }

        for (ConfigSource configSourceToAdd : configSourcesToAdd)
        {
            configSourceToAdd.setOnAttributeChange(this::onAttributeChange);
            allConfigSources.add(configSourceToAdd);
        }

        this.configSources = sortDescending(allConfigSources);
    }

    @Override
    public List<ConfigFilter> getConfigFilters()
    {
        return Collections.unmodifiableList(configFilters);
    }

    @Override
    public void addConfigFilter(ConfigFilter configFilter)
    {
        configFilters.add(configFilter);
    }

    @Override
    public String filterConfigValue(String key, String value, boolean forLog)
    {
        String filteredValue = value;

        for (ConfigFilter filter : configFilters)
        {
            filteredValue = forLog ?
                    filter.filterValueForLog(key, filteredValue) :
                    filter.filterValue(key, filteredValue);
        }
        return filteredValue;
    }


    @Override
    public ConfigResolver.UntypedResolver<String> resolve(String name)
    {
        return new TypedResolverImpl(this, name);
    }

    private ConfigSource[] sortDescending(List<ConfigSource> configSources)
    {
        Collections.sort(configSources, new Comparator<ConfigSource>()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public int compare(ConfigSource configSource1, ConfigSource configSource2)
            {
                int o1 = configSource1.getOrdinal();
                int o2 = configSource2.getOrdinal();
                if (o1 == o2)
                {
                    return configSource1.getConfigName().compareTo(configSource2.getConfigName());
                }
                return (o1 > o2) ? -1 : 1;
            }
        });
        return configSources.toArray(new ConfigSource[configSources.size()]);
    }

    public void onAttributeChange(Set<String> attributesChanged)
    {
        // this is to force an incremented lastChanged even on time glitches and fast updates
        long newLastChanged = System.nanoTime();
        lastChanged = lastChanged >= newLastChanged ? lastChanged++ : newLastChanged;
    }

    /**
     * @return the nanoTime when the last change got reported by a ConfigSource
     */
    public long getLastChanged()
    {
        return lastChanged;
    }

}
