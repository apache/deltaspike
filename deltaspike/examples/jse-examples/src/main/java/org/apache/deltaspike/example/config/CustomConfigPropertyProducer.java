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
package org.apache.deltaspike.example.config;

import org.apache.deltaspike.core.spi.config.BaseConfigPropertyProducer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.util.logging.Logger;

/**
 * Equivalent to a custom converter
 */
@ApplicationScoped
@SuppressWarnings("UnusedDeclaration")
public class CustomConfigPropertyProducer extends BaseConfigPropertyProducer
{
    private static final Logger LOG = Logger.getLogger(CustomConfigPropertyProducer.class.getName());

    @Produces
    @Dependent
    @Property2
    public Long produceProperty2(InjectionPoint injectionPoint)
    {
        String configuredValue = getStringPropertyValue(injectionPoint);

        if (configuredValue == null)
        {
            return null;
        }

        Property2 metaData = getAnnotation(injectionPoint, Property2.class);

        if (metaData.logValue())
        {
            LOG.info("value of property 2: " + configuredValue);
        }

        //X TODO integrate with the HandledHandler of DeltaSpike
        return Long.parseLong(configuredValue);
    }

    @Produces
    @Dependent
    @Property2WithInverseSupport
    public Long produceInverseProperty2(InjectionPoint injectionPoint)
    {
        String configuredValue = getStringPropertyValue(injectionPoint);

        if (configuredValue == null)
        {
            return null;
        }

        //X TODO integrate with the HandledHandler of DeltaSpike
        Long result = Long.parseLong(configuredValue);

        Property2WithInverseSupport metaData = getAnnotation(injectionPoint, Property2WithInverseSupport.class);

        if (metaData.inverseConvert())
        {
            return result * -1;
        }

        return result;
    }

    @Produces
    @Dependent
    @Location
    public LocationId produceLocationId(InjectionPoint injectionPoint)
    {
        String configuredValue = getStringPropertyValue(injectionPoint);

        /*
        //alternative to @ConfigProperty#defaultValue
        if (configuredValue == null)
        {
            return LocationId.LOCATION_X;
        }
        */
        return LocationId.valueOf(configuredValue.trim().toUpperCase());
    }
}
