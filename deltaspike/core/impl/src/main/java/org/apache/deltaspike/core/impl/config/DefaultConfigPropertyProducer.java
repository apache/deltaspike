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
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.spi.config.BaseConfigPropertyProducer;
import org.apache.deltaspike.core.api.config.ConfigProperty;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Supplier;

/**
 * This class contains producer methods for injecting
 * configuration provided with the {@link ConfigProperty}
 * annotation.
 */
@ApplicationScoped
@SuppressWarnings("UnusedDeclaration")
public class DefaultConfigPropertyProducer extends BaseConfigPropertyProducer
{
    @Produces
    @Dependent
    @ConfigProperty(name = "ignored") // we actually don't need the name
    public String produceStringConfiguration(InjectionPoint injectionPoint)
    {
        return getStringPropertyValue(injectionPoint);
    }

    @Produces
    @Dependent
    @ConfigProperty(name = "ignored") // we actually don't need the name
    public Class produceClassConfiguration(InjectionPoint injectionPoint)
    {
        return getPropertyWithException(injectionPoint, Class.class);
    }

    @Produces
    @Dependent
    @ConfigProperty(name = "ignored") // we actually don't need the name
    public Boolean produceBooleanConfiguration(InjectionPoint injectionPoint)
    {
        return getPropertyWithException(injectionPoint, Boolean.class);
    }

    @Produces
    @Dependent
    @ConfigProperty(name = "ignored") // we actually don't need the name
    public Integer produceIntegerConfiguration(InjectionPoint injectionPoint)
    {
        return getPropertyWithException(injectionPoint, Integer.class);
    }

    @Produces
    @Dependent
    @ConfigProperty(name = "ignored") // we actually don't need the name
    public Long produceLongConfiguration(InjectionPoint injectionPoint)
    {
        return getPropertyWithException(injectionPoint, Long.class);
    }

    @Produces
    @Dependent
    @ConfigProperty(name = "ignored") // we actually don't need the name
    public Float produceFloatConfiguration(InjectionPoint injectionPoint)
    {
        return getPropertyWithException(injectionPoint, Float.class);

    }

    @Produces
    @Dependent
    @ConfigProperty(name = "ignored") // we actually don't need the name
    public Double produceDoubleConfiguration(InjectionPoint injectionPoint)
    {
        return getPropertyWithException(injectionPoint, Double.class);
    }

    @Produces
    @Dependent
    @ConfigProperty(name = "ignore")
    public <C> Supplier<C> produceConfigSupplier(InjectionPoint injectionPoint)
    {
        ConfigProperty configProperty = getAnnotation(injectionPoint, ConfigProperty.class);

        if (configProperty == null)
        {
            throw new IllegalStateException("producer method called without @ConfigProperty being present!");
        }
        final Type injectionPointType = injectionPoint.getType();
        Type ipClass = null;
        if (injectionPointType instanceof ParameterizedType && ((ParameterizedType) injectionPointType).getActualTypeArguments().length == 1)
        {
            ipClass = ((ParameterizedType) injectionPointType).getActualTypeArguments()[0];
        }
        else
        {
            throw new IllegalStateException("Supplier for Configuration must be a Parameterized Type");
        }

        ConfigResolver.TypedResolver<C> resolver = asResolver(configProperty.name(),
            configProperty.defaultValue(),
            ipClass,
            configProperty.converter(),
            configProperty.parameterizedBy(),
            configProperty.projectStageAware(),
            configProperty.evaluateVariables());

        if (configProperty.cacheFor() > 0)
        {
            resolver.cacheFor(configProperty.cacheUnit(), configProperty.cacheFor());
        }

        return () -> resolver.getValue();
    }

    private <T> T getPropertyWithException(InjectionPoint ip, Type ipCls)
    {
        try
        {
            return getUntypedPropertyValue(ip, ipCls);
        }
        catch (RuntimeException rte)
        {
            ConfigProperty configProperty = getAnnotation(ip, ConfigProperty.class);
            throw new RuntimeException("Error while converting property '" + configProperty.name() +
                    "' happening in bean " + ip.getBean(), rte);
        }
    }
}
