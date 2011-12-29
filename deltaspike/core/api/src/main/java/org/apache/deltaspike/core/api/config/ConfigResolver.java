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
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.deltaspike.core.api.util.ClassUtils;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;

import javax.enterprise.inject.Typed;

/**
 * Resolve the configuration via their well defined ordinals.
 */
@Typed()
public final class ConfigResolver
{
    private ConfigResolver()
    {
        // this is a utility class which doesn't get instantiated.
    }

    /**
     * The content of this map will get lazily initiated and will hold the
     * sorted List of ConfigSources for each WebApp/EAR, etc (thus the
     * ClassLoader).
     */
    private static Map<ClassLoader, ConfigSource[]> configSources
            = new ConcurrentHashMap<ClassLoader, ConfigSource[]>();


    private static final Logger LOG = Logger.getLogger(ConfigResolver.class.getName());
    
    /**
     * Resolve the property value by going through the list of configured {@link ConfigSource}s
     * and use the one with the highest priority.
     * 
     * @param key the property key.
     * @return the configured property value from the {@link ConfigSource} with the highest ordinal.
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
     * Resolve all values for the given key, from all registered ConfigSources.
     * @param key
     * @return List with all found property values, sorted in order of their ordinal.
     */
    public static List<String> getAllPropertyValues(String key)
    {
        ConfigSource[] appConfigSources = getConfigSources();
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
            appConfigSources = sortConfigSources(resolveConfigSources());
            
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

    private static ConfigSource[] sortConfigSources(List<ConfigSource> configSources)
    {
        List<ConfigSource> sortedConfigSources = new ArrayList<ConfigSource>();
        for (ConfigSource cs : configSources)
        {
            int configOrder = cs.getOrdinal();

            int i;
            for (i = 0; i < sortedConfigSources.size(); i++)
            {
                int listConfigOrder = sortedConfigSources.get(i).getOrdinal();
                if (listConfigOrder > configOrder)
                {
                    // only go as far as we found a higher priority Properties file
                    break;
                }
            }
            sortedConfigSources.add(i, cs);
        }
        return sortedConfigSources.toArray(new ConfigSource[configSources.size()]);
    }
}
