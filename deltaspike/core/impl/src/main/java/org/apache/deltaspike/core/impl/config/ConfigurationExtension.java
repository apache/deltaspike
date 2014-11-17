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

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import java.util.ArrayList;
import java.util.List;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.PropertyFileConfig;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

/**
 * This extension handles {@link org.apache.deltaspike.core.api.config.PropertyFileConfig}s
 * provided by users.
 */
public class ConfigurationExtension implements Extension, Deactivatable
{
    private static final String CANNOT_CREATE_CONFIG_SOURCE_FOR_CUSTOM_PROPERTY_FILE_CONFIG =
        "Cannot create ConfigSource for custom property-file config ";

    private boolean isActivated = true;

    private List<Class<? extends PropertyFileConfig>> configSourcesClasses
        = new ArrayList<Class<?  extends PropertyFileConfig>>();


    @SuppressWarnings("UnusedDeclaration")
    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    @SuppressWarnings("UnusedDeclaration")
    public void collectUserConfigSources(@Observes ProcessAnnotatedType<? extends PropertyFileConfig> pat)
    {
        if (!isActivated)
        {
            return;
        }

        Class<? extends PropertyFileConfig> pcsClass = pat.getAnnotatedType().getJavaClass();
        if (pcsClass.isAnnotation() ||
            pcsClass.isInterface()  ||
            pcsClass.isSynthetic()  ||
            pcsClass.isArray()      ||
            pcsClass.isEnum()         )
        {
            // we only like to add real classes
            return;
        }

        configSourcesClasses.add(pcsClass);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void registerUserConfigSources(@Observes AfterDeploymentValidation adv)
    {
        if (!isActivated)
        {
            return;
        }

        List<ConfigSource> configSources = new ArrayList<ConfigSource>();

        for (Class<? extends PropertyFileConfig> propertyFileConfigClass : configSourcesClasses)
        {
            configSources.addAll(createPropertyConfigSource(propertyFileConfigClass));
        }

        // finally add all
        ConfigResolver.addConfigSources(configSources);
    }

    /**
     * This method triggers freeing of the ConfigSources.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void freeConfigSources(@Observes BeforeShutdown bs)
    {
        ConfigResolver.freeConfigSources();
    }

    /**
     * @return create an instance of the given {@link PropertyFileConfig} and return all it's ConfigSources.
     */
    private List<ConfigSource> createPropertyConfigSource(Class<? extends PropertyFileConfig> propertyFileConfigClass)
    {
        String fileName = "";
        try
        {
            PropertyFileConfig propertyFileConfig = propertyFileConfigClass.newInstance();
            fileName = propertyFileConfig.getPropertyFileName();
            EnvironmentPropertyConfigSourceProvider environmentPropertyConfigSourceProvider
                = new EnvironmentPropertyConfigSourceProvider(fileName, propertyFileConfig.isOptional());

            return environmentPropertyConfigSourceProvider.getConfigSources();
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(CANNOT_CREATE_CONFIG_SOURCE_FOR_CUSTOM_PROPERTY_FILE_CONFIG +
                propertyFileConfigClass.getName(), e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(CANNOT_CREATE_CONFIG_SOURCE_FOR_CUSTOM_PROPERTY_FILE_CONFIG +
                    propertyFileConfigClass.getName(), e);
        }
        catch (IllegalStateException e)
        {
            throw new IllegalStateException(
                propertyFileConfigClass.getName() + " points to an invalid file: '" + fileName + "'", e);
        }
    }
}
