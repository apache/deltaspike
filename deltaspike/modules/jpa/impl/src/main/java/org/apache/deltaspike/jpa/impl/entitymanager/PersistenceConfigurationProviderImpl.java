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
package org.apache.deltaspike.jpa.impl.entitymanager;

import javax.enterprise.context.ApplicationScoped;


import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.PropertyLoader;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.jpa.spi.entitymanager.PersistenceConfigurationProvider;

/**
 * Default implementation of the PersistenceConfigurationProvider.
 *
 */
@ApplicationScoped
public class PersistenceConfigurationProviderImpl implements PersistenceConfigurationProvider
{
    /**
     * A prefix which will be used for looking up more specific
     * information for a persistenceUnit.
     *
     * @see #addConfigProperties(Properties, String)
     */
    private static final String CONFIG_PREFIX = "deltaspike.persistence.config.";

    @Override
    public Properties getEntityManagerFactoryConfiguration(String persistenceUnitName)
    {
        Properties unitProperties = PropertyLoader.getProperties("persistence-" + persistenceUnitName);

        if (unitProperties == null)
        {
            unitProperties = new Properties();
        }

        // apply ConfigFilters to the configured values.
        for (Map.Entry entry : unitProperties.entrySet())
        {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            entry.setValue(ConfigResolver.filterConfigValue(key, value));
        }

        unitProperties = addConfigProperties(unitProperties, persistenceUnitName);

        // add spec expected attributes
        unitProperties.put("javax.persistence.bean.manager", BeanManagerProvider.getInstance().getBeanManager());

        return unitProperties;
    }


    /**
     * Load additional configuration from the Configuration system
     * and overload the basic settings with that info.
     *
     * The key is deltaspike.persistence.config.${persistenceUnitName}.${originalKey}
     *
     * @see #CONFIG_PREFIX
     * @since 1.8.0
     */
    protected Properties addConfigProperties(Properties unitProperties, String persistenceUnitName)
    {
        // we start with a copy of the original properties
        Properties mergedConfig = new Properties();
        mergedConfig.putAll(unitProperties);

        Set<String> allConfigKeys = ConfigResolver.getAllProperties().keySet();
        String unitPrefix = CONFIG_PREFIX + persistenceUnitName + ".";
        for (String configKey : allConfigKeys)
        {
            if (configKey.startsWith(unitPrefix))
            {
                mergedConfig.put(configKey.substring(unitPrefix.length()),
                        ConfigResolver.getProjectStageAwarePropertyValue(configKey));
            }
        }

        return mergedConfig;
    }
}
