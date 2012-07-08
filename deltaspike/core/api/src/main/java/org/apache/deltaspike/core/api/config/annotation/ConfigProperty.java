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
package org.apache.deltaspike.core.api.config.annotation;

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
 * <p>A small usage Example:
 * <pre>
 *   &#064;Inject &#064;ConfigProperty(name=&quot;database&quot;)
 *   private String configuredDatabase;
 * </pre>
 * </p>
 *
 * <p>See the sample in {@link org.apache.deltaspike.core.spi.config.BaseConfigPropertyProducer}
 * for how to implement own configuration injection.</p>
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
