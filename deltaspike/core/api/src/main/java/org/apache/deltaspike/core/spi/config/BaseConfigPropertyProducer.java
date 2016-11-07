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

import javax.enterprise.inject.spi.InjectionPoint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.BeanUtils;

/**
 * <p>This contains the fundamental parts for implementing own
 * ConfigProperty producers.</p>
 *
 * <h2>Providing own Converters and Type injection</h2>
 * <p>DeltaSpikes own configuration system natively supports only Strings.
 * If you'd like to apply own Converters or extract other types from those Strings,
 * you can simply do this by providing an own Qualifier and a simple
 * CDI producer method for it.</p>
 *
 * <p>First we write a simple Qualifier:
 * <pre>
 * &#064;Target({ PARAMETER, FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE })
 * &#064;Retention(RUNTIME)
 * &#064;ConfigProperty(named="unused") // the name
 * &#064;Qualifier
 * public @interface NumberConfig
 * {
 *     &#064;Nonbinding
 *     boolean name(); // the name of the configuration-key to lookup the value
 *
 *     &#064;Nonbinding
 *     String defaultValue() default ConfigProperty.NULL;
 *
 *     &#064;Nonbinding
 *     boolean pattern(); // the pattern for NumberFormatter
 * }
 * </pre>
 * </p>
 *
 * <p>The producer method implementation is pretty easy as well:
 * <pre>
 * &#064;ApplicationScoped
 * public class NumberConfigProducer extends BaseConfigPropertyProducer
 * {
 *     &#064;Produces
 *     &#064;Dependent
 *     &#064;NumberConfig
 *     public Float produceNumberConfig(InjectionPoint injectionPoint)
 *     {
 *         // resolve the annotation
 *         NumberConfig metaData = getAnnotation(injectionPoint, NumberConfig.class);

 *         // get the configured value from the underlying configuration system
 *         String configuredValue = getPropertyValue(metaData.name(), metaData.defaultValue());
 *         if (configuredValue == null)
 *         {
 *             return null;
 *         }
 *
 *         // format according to the given pattern
 *         DecimalFormat df = new DecimalFormat(metaData.pattern(), new DecimalFormatSymbols(Locale.US));
 *         return df.parse(configuredValue).floatValue();
 *     }
 * }
 * </pre>
 * </p>

 */
public abstract class BaseConfigPropertyProducer
{
    /**
     * <p>Inspects the given InjectionPoint and search for a {@link ConfigProperty}
     * annotation or an Annotation with a {@link ConfigProperty} meta-Annotation.
     * The name and defaultValue information will be used to resolve the
     * configured value.</p>
     *
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

        return getPropertyValue(injectionPoint, String.class);
    }

    protected <T> T getPropertyValue(InjectionPoint injectionPoint, Class<T> ipCls)
    {
        return getUntypedPropertyValue(injectionPoint, ipCls);
    }

    protected <T> T getUntypedPropertyValue(InjectionPoint injectionPoint, Type ipCls)
    {
        ConfigProperty configProperty = getAnnotation(injectionPoint, ConfigProperty.class);

        if (configProperty == null)
        {
            throw new IllegalStateException("producer method called without @ConfigProperty being present!");
        }

        return readEntry(configProperty.name(), configProperty.defaultValue(), ipCls,
                configProperty.converter(), configProperty.parameterizedBy(),
                configProperty.projectStageAware(), configProperty.evaluateVariables());
    }

    /**
     * @param propertyName the name of the property key
     * @param defaultValue the default value to return if no configured property is found or
     *                     {@link ConfigProperty#NULL} if no default value should be returned.
     * @return the configured value or the defaultValue according to the NULL logic.
     */
    protected String getPropertyValue(String propertyName, String defaultValue)
    {
        String configuredValue;
        if (ConfigProperty.NULL.equals(defaultValue))
        {
            // no special defaultValue has been configured
            configuredValue = ConfigResolver.getProjectStageAwarePropertyValue(propertyName);
        }
        else
        {
            configuredValue = ConfigResolver.getProjectStageAwarePropertyValue(propertyName, defaultValue);
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
        return BeanUtils.extractAnnotation(injectionPoint.getAnnotated(), targetType);
    }

    public <T> T readEntry(final String key, final String stringDefault, final Type ipCls,
                           final Class<? extends ConfigResolver.Converter> converterType,
                           final String parameterizedBy, final boolean projectStageAware, final boolean evaluate)
    {
        final ConfigResolver.TypedResolver<T> resolver = asResolver(
                key, stringDefault, ipCls, converterType, parameterizedBy, projectStageAware, evaluate);
        return resolver.getValue();
    }

    public <T> ConfigResolver.TypedResolver<T> asResolver(final String key, final String stringDefault,
                                                          final Type ipCls,
                                                          final Class<? extends ConfigResolver.Converter> converterType,
                                                          final String parameterizedBy,
                                                          final boolean projectStageAware, final boolean evaluate)
    {
        final ConfigResolver.UntypedResolver<String> untypedResolver = ConfigResolver.resolve(key);
        final ConfigResolver.TypedResolver<T> resolver =
                (ConfigResolver.Converter.class == converterType ?
                        untypedResolver.as(Class.class.cast(ipCls)) :
                        untypedResolver.as(ipCls, BeanProvider.getContextualReference(converterType)))
                        .withCurrentProjectStage(projectStageAware);
        if (!ConfigProperty.NULL.equals(stringDefault))
        {
            resolver.withStringDefault(stringDefault);
        }
        if (!ConfigProperty.NULL.equals(parameterizedBy))
        {
            resolver.parameterizedBy(parameterizedBy);
        }
        return resolver.evaluateVariables(evaluate);
    }
}
