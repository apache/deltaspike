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

import org.apache.deltaspike.core.api.converter.Converter;

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
 * A small Example:
 * <pre>
 *   &#064;Inject &#064;ConfigProperty(name=&quot;database&quot;)
 *   private String configuredDatabase;
 * </pre>
 *
 * @see org.apache.deltaspike.core.api.config.ConfigResolver
 */
@Target({ PARAMETER, FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
@Qualifier
public @interface ConfigProperty
{
    /**
     * Name/key of the property
     * @return name of the property
     */
    String name();

    /**
     * Custom converter
     * @return custom converter or default marker
     */
    @Nonbinding
    Class<? extends Converter> converter() default Converter.class;

    /**
     * Per default all properties are validated during the bootstrapping process of the CDI container.
     * If it can't be resolved, the bootstrapping will fail.
     *
     * Set it to true if the property will be set dynamically e.g. during the bootstrapping process and
     * it will be stored in a dynamic data-store like data-base.
     *
     * @return true if the property has to be available from the very beginning, false otherwise
     */
    @Nonbinding
    boolean eager() default true;
}
