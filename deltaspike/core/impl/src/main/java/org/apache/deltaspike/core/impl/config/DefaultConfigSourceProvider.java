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

import org.apache.deltaspike.core.api.config.PropertyFileConfig;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;
import org.apache.deltaspike.core.util.ServiceUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation which uses:
 * <ol>
 * <li>SystemPropertyConfigSource</li>
 * <li>EnvironmentPropertyConfigSource</li>
 * <li>LocalJndiConfigSource</li>
 * <li>PropertyFileConfigSource</li>
 * </ol>
 */
public class DefaultConfigSourceProvider implements ConfigSourceProvider
{
    private static final Logger LOG = Logger.getLogger(DefaultConfigSourceProvider.class.getName());

    private static final String PROPERTY_FILE_NAME = "apache-deltaspike.properties";
    private static final String PROPERTY_FILE_RESOURCE = "META-INF/" + PROPERTY_FILE_NAME;
    private static final String PROPERTY_FILE_HOME_NAME = "/.deltaspike/" + PROPERTY_FILE_NAME;

    private List<ConfigSource> configSources = new ArrayList<ConfigSource>();

    /**
     * Default constructor which adds the {@link ConfigSource} implementations which are supported by default
     */
    public DefaultConfigSourceProvider()
    {
        configSources.add(new SystemPropertyConfigSource());
        configSources.add(new EnvironmentPropertyConfigSource());
        configSources.add(new LocalJndiConfigSource());

        addUserHomeConfigSource();

        EnvironmentPropertyConfigSourceProvider epcsp =
            new EnvironmentPropertyConfigSourceProvider(PROPERTY_FILE_RESOURCE, true);
        configSources.addAll(epcsp.getConfigSources());

        registerPropertyFileConfigs();
    }


    /**
     * Add a ConfigSource for files in the user home folder IF it exists!
     * The location is ~/.deltaspike/apache-deltaspike.properties
     */
    private void addUserHomeConfigSource()
    {
        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isEmpty())
        {
            File dsHome = new File(userHome, PROPERTY_FILE_HOME_NAME);
            try
            {
                if (dsHome.exists())
                {
                    try
                    {
                        ConfigSource dsHomeConfigSource = new PropertyFileConfigSource(dsHome.toURI().toURL());
                        configSources.add(dsHomeConfigSource);
                        LOG.log(Level.INFO, "Reading configuration from {}", dsHome.getAbsolutePath());
                    }
                    catch (MalformedURLException e)
                    {
                        LOG.log(Level.WARNING, "Could not read configuration from " + dsHome.getAbsolutePath(), e);
                    }

                }
            }
            catch (SecurityException se)
            {
                LOG.log(Level.INFO, "Not allowed to check if directory {} exists", dsHome.getPath());
            }
        }
    }


    /**
     * Load all {@link PropertyFileConfig}s which are registered via
     * {@code java.util.ServiceLoader}.
     */
    private void registerPropertyFileConfigs()
    {
        List<? extends PropertyFileConfig> propertyFileConfigs =
                ServiceUtils.loadServiceImplementations(PropertyFileConfig.class);
        for (PropertyFileConfig propertyFileConfig : propertyFileConfigs)
        {
            EnvironmentPropertyConfigSourceProvider environmentPropertyConfigSourceProvider
                = new EnvironmentPropertyConfigSourceProvider(propertyFileConfig.getPropertyFileName(),
                    propertyFileConfig.isOptional());

            configSources.addAll(environmentPropertyConfigSourceProvider.getConfigSources());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigSource> getConfigSources()
    {
        return configSources;
    }
}
