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
package org.apache.deltaspike.test.core.api.config.injectable;

import org.apache.deltaspike.core.spi.config.BaseConfigPropertyProducer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Sample producer for {@link CustomConfigAnnotationWithMetaData}
 */
@ApplicationScoped
public class CustomConfigAnnotationWithMetaDataProducer extends BaseConfigPropertyProducer
{
    @Produces
    @Dependent
    @CustomConfigAnnotationWithMetaData
    public Integer produceIntegerCustomConfig(InjectionPoint injectionPoint)
    {
        String configuredValue = getStringPropertyValue(injectionPoint);

        if (configuredValue == null || configuredValue.length() == 0)
        {
            return 0;
        }

        Integer result = Integer.parseInt(configuredValue);

        CustomConfigAnnotationWithMetaData metaData =
                getAnnotation(injectionPoint, CustomConfigAnnotationWithMetaData.class);

        if (metaData != null && metaData.inverseConvert())
        {
            return result * -1;
        }

        return result;
    }

    @Produces
    @Dependent
    @CustomConfigAnnotationWithMetaData
    public Long produceLongCustomConfig(InjectionPoint injectionPoint)
    {
        String configuredValue = getStringPropertyValue(injectionPoint);

        if (configuredValue == null || configuredValue.length() == 0)
        {
            return 0L;
        }

        Long result = Long.parseLong(configuredValue);

        CustomConfigAnnotationWithMetaData metaData =
                getAnnotation(injectionPoint, CustomConfigAnnotationWithMetaData.class);

        if (metaData != null && metaData.inverseConvert())
        {
            return result * -1L;
        }

        return result;
    }
}
