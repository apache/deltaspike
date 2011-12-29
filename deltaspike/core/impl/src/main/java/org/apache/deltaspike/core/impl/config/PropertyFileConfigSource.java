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

import org.apache.deltaspike.core.api.util.ClassUtils;
import org.apache.deltaspike.core.spi.config.ConfigSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * {@link ConfigSource} which uses
 * apache-deltaspike.properties btw. /META-INF/apache-deltaspike.properties for the lookup
 */
class PropertyFileConfigSource extends ConfigSource
{
    private static Map<ClassLoader, Map<String, String>> propertyCache =
            new ConcurrentHashMap<ClassLoader, Map<String, String>>();

    private static final String FILE_NAME = "apache-deltaspike";

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getDefaultOrdinal()
    {
        return 400;
    }

    /**
     * The given key gets used for a lookup via a properties file
     *
     * @param key for the property
     * @return value for the given key or null if there is no configured value
     */
    @Override
    public String getPropertyValue(String key)
    {
        Map<String, String> cache = getPropertyCache();

        String configuredValue = cache.get(key);

        if ("".equals(configuredValue))
        {
            return null;
        }

        ResourceBundle resourceBundle;

        try
        {
            resourceBundle = loadResourceBundleFromClasspath();

            if (resourceBundle.containsKey(key))
            {
                configuredValue = resourceBundle.getString(key);
                cache.put(key, configuredValue);
            }

            if (configuredValue == null)
            {
                Properties properties = loadPropertiesFromMetaInf();

                if (properties != null)
                {
                    configuredValue = properties.getProperty(key);
                    cache.put(key, configuredValue);
                }
            }

            if (configuredValue == null)
            {
                cache.put(key, "");
                return null;
            }
        }
        catch (Exception e)
        {
            cache.put(key, "");
            return null;
        }
        return configuredValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigName()
    {
        //X TODO should we split the impl. to have a better name?
        return FILE_NAME + ".properties";
    }

    private ResourceBundle loadResourceBundleFromClasspath()
    {
        try
        {
            return ResourceBundle.getBundle(FILE_NAME, Locale.getDefault(), ClassUtils.getClassLoader(null));
        }
        catch (MissingResourceException e)
        {
            //it was just a try
            return null;
        }
    }

    private Map<String, String> getPropertyCache()
    {
        ClassLoader classLoader = ClassUtils.getClassLoader(null);
        Map<String, String> cache = propertyCache.get(classLoader);

        if (cache == null)
        {
            cache = new ConcurrentHashMap<String, String>();
            propertyCache.put(classLoader, cache);
        }
        return cache;
    }

    private Properties loadPropertiesFromMetaInf()
    {
        String resourceName = "META-INF/" + FILE_NAME + ".properties";
        Properties properties = null;

        ClassLoader classLoader = ClassUtils.getClassLoader(resourceName);
        InputStream inputStream = classLoader.getResourceAsStream(resourceName);

        if (inputStream != null)
        {
            properties = new Properties();

            try
            {
                properties.load(inputStream);
            }
            catch (IOException e)
            {
                return null;
            }
            finally
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    LOG.log(Level.WARNING, "Failed to close " + resourceName, e);
                }
            }
        }

        return properties;
    }
}
