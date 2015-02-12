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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.PropertyFileConfig;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;

/**
 * This extension handles {@link org.apache.deltaspike.core.api.config.PropertyFileConfig}s
 * provided by users.
 */
public class ConfigurationExtension implements Extension, Deactivatable
{
    private static final String CANNOT_CREATE_CONFIG_SOURCE_FOR_CUSTOM_PROPERTY_FILE_CONFIG =
        "Cannot create ConfigSource for custom property-file config ";

    /**
     * This is a trick for EAR scenarios in some containers.
     * They e.g. boot up the shared EAR lib with the ear ClassLoader.
     * Thus any {@link org.apache.deltaspike.core.api.config.PropertyFileConfig} configuration will just get
     * activated for this very single EAR ClassLoader but <em>not</em> for all the webapps.
     * But if I have a property file in a jar in the shared EAR lib then I most likely also like to get it
     * if I call this from my webapp (TCCL).
     * So we also automatically register all the PropertyFileConfigs we found in the 'parent BeanManager'
     * as well.
     */
    private static Map<ClassLoader, List<Class<? extends PropertyFileConfig>>> detectedParentPropertyFileConfigs
        = new ConcurrentHashMap<ClassLoader, List<Class<? extends PropertyFileConfig>>>();

    private boolean isActivated = true;

    private List<Class<? extends PropertyFileConfig>> propertyFileConfigClasses
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

        propertyFileConfigClasses.add(pcsClass);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void registerUserConfigSources(@Observes AfterDeploymentValidation adv)
    {
        if (!isActivated)
        {
            return;
        }

        // create a local copy with all the collected PropertyFileConfig
        Set<Class<? extends PropertyFileConfig>> allPropertyFileConfigClasses
            = new HashSet<Class<? extends PropertyFileConfig>>(this.propertyFileConfigClasses);

        // now add any PropertyFileConfigs from a 'parent BeanManager'
        // we start with the current TCCL
        ClassLoader currentClassLoader = ClassUtils.getClassLoader(null);
        addParentPropertyFileConfigs(currentClassLoader, allPropertyFileConfigClasses);

        // now let's add our own PropertyFileConfigs to the detected ones.
        // because maybe WE are a parent BeanManager ourselves!
        if (!this.propertyFileConfigClasses.isEmpty())
        {
            detectedParentPropertyFileConfigs.put(currentClassLoader, this.propertyFileConfigClasses);
        }

        // collect all the ConfigSources from our PropertyFileConfigs
        List<ConfigSource> configSources = new ArrayList<ConfigSource>();
        for (Class<? extends PropertyFileConfig> propertyFileConfigClass : allPropertyFileConfigClasses)
        {
            configSources.addAll(createPropertyConfigSource(propertyFileConfigClass));
        }


        // finally add all
        ConfigResolver.addConfigSources(configSources);
    }

    /**
     * Add all registered PropertyFileConfigs which got picked up in a parent ClassLoader already
     */
    private void addParentPropertyFileConfigs(ClassLoader currentClassLoader,
                                              Set<Class<? extends PropertyFileConfig>> propertyFileConfigClasses)
    {
        if (currentClassLoader.getParent() == null)
        {
            return;
        }

        for (Map.Entry<ClassLoader, List<Class<? extends PropertyFileConfig>>> classLoaderListEntry :
                detectedParentPropertyFileConfigs.entrySet())
        {
            if (currentClassLoader.getParent().equals(classLoaderListEntry.getKey()))
            {
                // if this is the direct parent ClassLoader then lets add those PropertyFileConfigs.
                propertyFileConfigClasses.addAll(classLoaderListEntry.getValue());

                // even check further parents
                addParentPropertyFileConfigs(classLoaderListEntry.getKey(), propertyFileConfigClasses);

                // and be done. There can only be a single parent CL...
                return;
            }
        }
    }

    /**
     * This method triggers freeing of the ConfigSources.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void freeConfigSources(@Observes BeforeShutdown bs)
    {
        ConfigResolver.freeConfigSources();
        detectedParentPropertyFileConfigs.remove(ClassUtils.getClassLoader(null));
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
