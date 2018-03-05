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
import org.apache.deltaspike.core.spi.config.ConfigFilter;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;
import org.apache.deltaspike.core.util.ServiceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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


    @Override
    public ConfigSource[] getConfigSources()
    {
        return configSources;
    }

    @Override
    public void addConfigSources(List<ConfigSource> configSourcesToAdd)
    {
        List<ConfigSource> allConfigSources = new ArrayList<>();
        if (this.configSources != null)
        {
            for (ConfigSource configSource : this.configSources)
            {
                allConfigSources.add(configSource);
            }
        }

        allConfigSources.addAll(configSourcesToAdd);

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

}
