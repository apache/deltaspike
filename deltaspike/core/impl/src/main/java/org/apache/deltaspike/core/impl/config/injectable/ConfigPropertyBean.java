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
package org.apache.deltaspike.core.impl.config.injectable;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.converter.Converter;
import org.apache.deltaspike.core.api.converter.MetaDataAwareConverter;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.spi.converter.ConverterFactory;
import org.apache.deltaspike.core.util.ClassUtils;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ConfigPropertyBean<T> implements Bean<T>, Serializable
{
    private static final long serialVersionUID = 219378243102320371L;

    private final Class<?> beanType;
    private final Set<Annotation> qualifiers;
    private final Set<Type> types;
    private final ConfigProperty configProperty;
    private final Annotation customQualifier;

    public ConfigPropertyBean(Type targetType, ConfigProperty configProperty, Annotation customQualifier)
    {
        this.configProperty = configProperty;

        qualifiers = new HashSet<Annotation>();

        if (customQualifier != null)
        {
            qualifiers.add(customQualifier);
        }
        else
        {
            qualifiers.add(configProperty);
        }

        this.customQualifier = customQualifier;

        beanType = (Class<?>) targetType;

        types = new HashSet<Type>();
        types.add(beanType);
    }

    @Override
    public Set<Type> getTypes()
    {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers()
    {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return Dependent.class;
    }

    @Override
    public String getName()
    {
        //if we would support bean-names, we couldn't support e.g. multiple custom annotations for the same property
        return null;
    }

    @Override
    public boolean isNullable()
    {
        return false;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints()
    {
        return Collections.emptySet();
    }

    @Override
    public Class<?> getBeanClass()
    {
        return beanType;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes()
    {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative()
    {
        return false;
    }

    @Override
    public T create(CreationalContext<T> creationalContext)
    {
        ConverterFactory converterFactory = BeanProvider.getContextualReference(ConverterFactory.class, false);

        //TODO add support for collections, ...
        //TODO discuss handling of null values
        String configuredValue = ConfigResolver.getPropertyValue(configProperty.name());

        Converter converter;

        if (Converter.class.equals(configProperty.converter()))
        {
            //TODO add exception handling, if we throw an exception for an unknown converter
            converter = converterFactory.create(String.class, (Class<?>) beanType,
                    customQualifier != null ? customQualifier.annotationType() : null);
        }
        else
        {
            converter = BeanProvider.getContextualReference(configProperty.converter(), true);

            if (converter == null)
            {
                converter = ClassUtils.tryToInstantiateClass(configProperty.converter());
            }
        }

        if (converter == null)
        {
            if (String.class.isAssignableFrom(beanType))
            {
                return (T)configuredValue;
            }
            
            throw new IllegalStateException("can't find config for " +
                    String.class.getName() + " -> " + beanType.getName());
        }

        if (customQualifier != null && converter instanceof MetaDataAwareConverter)
        {
            return (T) ((MetaDataAwareConverter) converter).convert(configuredValue, customQualifier);
        }
        //noinspection unchecked
        return (T) converter.convert(configuredValue);
    }

    @Override
    public void destroy(T configPropertyProducer,
                        CreationalContext<T> configPropertyProducerCreationalContext)
    {
        //no need to destroy a configured value
    }
}
