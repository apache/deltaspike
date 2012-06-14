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
package org.apache.deltaspike.core.spi.config;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;

/**
 * This contains the fundamental parts for implementing own
 * ConfigProperty producers.
 *
 * TODO: add documentation
 */
public abstract class BaseConfigPropertyProducer
{
    /**
     * @param injectionPoint current injection point
     * @return the configured value for the given InjectionPoint
     */
    protected String getStringPropertyValue(InjectionPoint injectionPoint)
    {
        ConfigProperty configProperty = getAnnotation(injectionPoint, ConfigProperty.class);

        if (configProperty == null)
        {
            throw new IllegalStateException("producer method called without @ConfigProperty being present!");
        }

        String configuredValue;
        String defaultValue = configProperty.defaultValue();

        if (ConfigProperty.NULL.equals(defaultValue))
        {
            // no special defaultValue has been configured
            configuredValue = ConfigResolver.getPropertyValue(configProperty.name());
        }
        else
        {
            configuredValue = ConfigResolver.getPropertyValue(configProperty.name(), defaultValue);
        }

        return configuredValue;
    }

    /**
     * @param injectionPoint current injection point
     * @param targetType target type
     * @param <T> type
     * @return annotation instance extracted from the injection point which matches the given type
     */
    protected <T extends Annotation> T getAnnotation(InjectionPoint injectionPoint, Class<T> targetType)
    {
        Annotated annotated = injectionPoint.getAnnotated();

        T result = annotated.getAnnotation(targetType);

        if (result == null)
        {
            for (Annotation annotation : annotated.getAnnotations())
            {
                result = annotation.annotationType().getAnnotation(targetType);

                if (result != null)
                {
                    break;
                }
            }
        }

        return result;
    }
}
