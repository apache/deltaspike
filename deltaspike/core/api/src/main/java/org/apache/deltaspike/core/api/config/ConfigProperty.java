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
package org.apache.deltaspike.core.api.config;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>This Qualifier allows to use the DeltaSpike configuration mechanism
 * via simple injection.</p>
 *
 * <p>Example 1:
 * <pre>
 *   &#064;Inject &#064;ConfigProperty(name=&quot;locationId&quot;)
 *   private String locationId;
 * </pre>
 * </p>
 *
 * <p>Example 2 (the type-safe alternative):
 *
 * <pre>
 *   &#064;Target({ FIELD, METHOD })
 *   &#064;Retention(RUNTIME)
 *   &#064;ConfigProperty(name = "locationId")
 *   // alternative to null check in the producer:
 *   // &#064;ConfigProperty(name = "locationId", defaultValue = "LOCATION_X")
 *   &#064;Qualifier
 *   public &#064;interface Location
 *   {
 *   }
 * </pre>
 * </p>
 *
 * Depending on the producer it's possible to use a String or a custom type like an enum at the injection point.
 * <p/>
 * With a String:
 * <pre>
 *   &#064;Location
 *   private String locationId;
 * </pre>
 *
 * With a custom type:
 * <pre>
 *   &#064;Inject
 *   &#064;Location
 *   private LocationId locationId;
 * </pre>
 * <p>In any case a custom producer is needed.
 * {@link org.apache.deltaspike.core.spi.config.BaseConfigPropertyProducer} can be used as an base for custom
 * producers.
 * Producer for the configured String:
 * <pre>
 *   &#064;ApplicationScoped
 *   public class CustomConfigPropertyProducer extends BaseConfigPropertyProducer
 *   {
 *     &#064;Produces
 *     &#064;Dependent
 *     &#064;Location
 *     public String produceLocationId(InjectionPoint injectionPoint)
 *     {
 *       String configuredValue = getStringPropertyValue(injectionPoint);
 *       if (configuredValue == null)
 *       {
 *         return null;
 *       }
 *       return configuredValue;
 *     }
 *   }
 * </pre>
 *
 * Producer for a custom type:
 * <pre>
 *   &#064;ApplicationScoped
 *   public class CustomConfigPropertyProducer extends BaseConfigPropertyProducer
 *   {
 *     &#064;Produces
 *     &#064;Dependent
 *     &#064;Location
 *     public LocationId produceLocationId(InjectionPoint injectionPoint)
 *     {
 *       String configuredValue = getStringPropertyValue(injectionPoint);
 *       if (configuredValue == null)
 *       {
 *         return null;
 *       }
 *       return LocationId.valueOf(configuredValue.trim().toUpperCase());
 *     }
 *   }
 * </pre>
 *
 * @see org.apache.deltaspike.core.api.config.ConfigResolver
 * @see org.apache.deltaspike.core.spi.config.BaseConfigPropertyProducer
 */
@Target({ PARAMETER, FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
@Qualifier
public @interface ConfigProperty
{
    /**
     * This constant is a workaround for the java restriction that Annotation values
     * cannot be set to null. Do not use this String in your configuration...
     */
    String NULL = "org.apache.deltaspike.NullValueMarker";

    /**
     * Name/key of the property
     * @return name of the property
     */
    @Nonbinding
    String name();

    /**
     * <b>Optional</b> default value.
     * @return the default value which should be used if no config value could be found
     */
    @Nonbinding
    String defaultValue() default NULL;
}
