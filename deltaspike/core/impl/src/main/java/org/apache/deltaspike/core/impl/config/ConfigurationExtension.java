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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.PropertyFileConfig;
import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.spi.config.BaseConfigPropertyProducer;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.spi.config.ConfigValidator;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ServiceUtils;

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

    private final Set<Type> dynamicConfigTypes = new HashSet<Type>();
    private Bean<DynamicBeanProducer> dynamicProducer;

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

        if (pat.getAnnotatedType().isAnnotationPresent(Exclude.class))
        {
            // We only pick up PropertyFileConfigs if they are not excluded
            // This can be the case for PropertyFileConfigs which are registered via java.util.ServiceLoader
            return;
        }

        propertyFileConfigClasses.add(pcsClass);
    }

    public void findDynamicProducer(@Observes ProcessBean<DynamicBeanProducer> processBean)
    {
        dynamicProducer = processBean.getBean();
    }

    public void collectDynamicTypes(@Observes ProcessBean<?> processBean)
    {
        for (final InjectionPoint ip : processBean.getBean().getInjectionPoints())
        {
            final ConfigProperty annotation = ip.getAnnotated().getAnnotation(ConfigProperty.class);
            if (annotation == null || annotation.converter() == ConfigResolver.Converter.class)
            {
                continue;
            }

            dynamicConfigTypes.add(ip.getType());
        }
    }

    public void addDynamicBean(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager bm)
    {
        if (dynamicProducer != null && !dynamicConfigTypes.isEmpty())
        {
            afterBeanDiscovery.addBean(new DynamicBean(dynamicProducer, dynamicConfigTypes));
        }
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

        processConfigurationValidation(adv);
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

    protected void processConfigurationValidation(AfterDeploymentValidation adv)
    {
        for (ConfigValidator configValidator : ServiceUtils.loadServiceImplementations(ConfigValidator.class))
        {
            Set<String> violations = configValidator.processValidation();

            if (violations == null)
            {
                continue;
            }

            for (String violation : violations)
            {
                adv.addDeploymentProblem(new IllegalStateException(violation));
            }
        }
    }

    @ApplicationScoped
    @Typed(DynamicBeanProducer.class) // used as an internal bean
    static class DynamicBeanProducer extends BaseConfigPropertyProducer
    {
        @Produces
        @ConfigProperty(name = "ignored")
        public Object create(final InjectionPoint ip)
        {
            return super.getUntypedPropertyValue(ip, ip.getType());
        }
    }

    @Typed
    private static final class DynamicBean<T> implements Bean<T>
    {
        private final Bean<T> producer;
        private final Set<Type> types;

        private DynamicBean(final Bean<T> producer, final Set<Type> types)
        {
            this.producer = producer;
            this.types = types;
        }

        @Override
        public Set<Type> getTypes()
        {
            return types;
        }

        @Override
        public Set<Annotation> getQualifiers()
        {
            return producer.getQualifiers();
        }

        @Override
        public Class<? extends Annotation> getScope()
        {
            return producer.getScope();
        }

        @Override
        public String getName()
        {
            return producer.getName();
        }

        @Override
        public boolean isNullable()
        {
            return producer.isNullable();
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints()
        {
            return producer.getInjectionPoints();
        }

        @Override
        public Class<?> getBeanClass()
        {
            return producer.getBeanClass();
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes()
        {
            return producer.getStereotypes();
        }

        @Override
        public boolean isAlternative()
        {
            return producer.isAlternative();
        }

        @Override
        public T create(final CreationalContext<T> creationalContext)
        {
            return producer.create(creationalContext);
        }

        @Override
        public void destroy(final T t, final CreationalContext<T> creationalContext)
        {
            producer.destroy(t, creationalContext);
        }
    }
}
