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
package org.apache.deltaspike.core.api.jmx;

import javax.enterprise.inject.Stereotype;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation marks a CDI managed bean as exported through JMX.
 */
@Stereotype

@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface MBean
{
    /**
     * @return the category to use if no objectName was specified. Default is "org.apache.deltaspike" and can be
     *         overriden either directly by the value or by a key used to resolve a value using
     *         {@link org.apache.deltaspike.core.api.config.ConfigResolver}. It is a key if the value is between
     *         brackets. Default key is "org.apache.deltaspike.mbean.category".
     */
    String category() default "{org.apache.deltaspike.mbean.category}";

    /**
     * @return the name of the bean used if no objectName was specified. It is used with category value to create the
     *         MBean {@link javax.management.ObjectName} using the following pattern:
     *         &lt;category&gt;:type=MBeans,name=&lt;name&gt;
     */
    String name() default "";

    /**
     * @return the properties part of the objectName if no objectName was specified.
     *         If name and type are specified this segment is concatenated after.
     */
    String properties() default "";

    /**
     * @return the type to use if no objectName was specified. Default is <pre>MBeans</pre> and can be
     *         overriden either directly by the value or by a key used to resolve a value using
     *         {@link org.apache.deltaspike.core.api.config.ConfigResolver}. It is a key if the value is between
     *         brackets.
     */
    String type() default "";

    /**
     * @return the direct object name used to export the decorated bean.
     */
    String objectName() default "";

    /**
     * @return the description used to describe the JMX bean.
     */
    String description() default "";
}

