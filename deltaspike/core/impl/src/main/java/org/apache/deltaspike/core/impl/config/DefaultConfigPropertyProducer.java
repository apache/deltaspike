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

import org.apache.deltaspike.core.spi.config.BaseConfigPropertyProducer;
import org.apache.deltaspike.core.api.config.ConfigProperty;

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
    public Integer produceIntegerConfiguration(InjectionPoint injectionPoint)
    {
        String configuredValue = getStringPropertyValue(injectionPoint);
        if (configuredValue == null)
        {
            return null;
        }

        try
        {
            return Integer.parseInt(configuredValue);
        }
        catch (NumberFormatException nfe)
        {
            ConfigProperty configProperty = getAnnotation(injectionPoint, ConfigProperty.class);
            throw new RuntimeException("Error while converting Integer property '" + configProperty.name() +
                    "' value: " + configuredValue + " happening in bean " + injectionPoint.getBean() , nfe);
        }

    }

    @Produces
    @Dependent
    @ConfigProperty(name = "ignored") // we actually don't need the name
    public Long produceLongConfiguration(InjectionPoint injectionPoint)
    {
        String configuredValue = getStringPropertyValue(injectionPoint);
        if (configuredValue == null)
        {
            return null;
        }

        try
        {
            return Long.parseLong(configuredValue);
        }
        catch (NumberFormatException nfe)
        {
            ConfigProperty configProperty = getAnnotation(injectionPoint, ConfigProperty.class);
            throw new RuntimeException("Error while converting Long property '" + configProperty.name() +
                    "' value: " + configuredValue + " happening in bean " + injectionPoint.getBean() , nfe);
        }
    }

    @Produces
    @Dependent
    @ConfigProperty(name = "ignored") // we actually don't need the name
    public Boolean produceBooleanConfiguration(InjectionPoint injectionPoint)
    {
        String configuredValue = getStringPropertyValue(injectionPoint);
        if (configuredValue == null)
        {
            return null;
        }

        Boolean isTrue = "TRUE".equalsIgnoreCase(configuredValue);
        isTrue |= "1".equalsIgnoreCase(configuredValue);
        isTrue |= "YES".equalsIgnoreCase(configuredValue);
        isTrue |= "Y".equalsIgnoreCase(configuredValue);
        isTrue |= "JA".equalsIgnoreCase(configuredValue);
        isTrue |= "J".equalsIgnoreCase(configuredValue);
        isTrue |= "OUI".equalsIgnoreCase(configuredValue);

        return isTrue;
    }

    @Produces
    @Dependent
    @ConfigProperty(name = "ignored") // we actually don't need the name
    public Float produceFloatConfiguration(InjectionPoint injectionPoint)
    {
        String configuredValue = getStringPropertyValue(injectionPoint);

        if (configuredValue == null)
        {
            return null;
        }

        try
        {
            return Float.parseFloat(configuredValue);
        }
        catch (NumberFormatException nfe)
        {
            ConfigProperty configProperty = getAnnotation(injectionPoint, ConfigProperty.class);
            throw new RuntimeException("Error while converting Float property '" + configProperty.name() +
                    "' value: " + configuredValue + " happening in bean " + injectionPoint.getBean() , nfe);
        }

    }
}
