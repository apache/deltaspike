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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Describes a JMX operation or attribute, when used on a method or a field, respectively.
 * <p>
 * Used on a method it describes a JMX operation with an optional description.
 * An exception thrown by the method will be wrapped in a {@link javax.management.MBeanException}
 * unless it already is a {@code MBeanException}.
 * <p>
 * Used on a field it describes a JMX attribute. This attribute is readable if a getter on this field is available and
 * writable if a setter is found.
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
@Documented
public @interface JmxManaged
{
    /**
     * @return the description either of the operation or the attribute exported through JMX.
     */
    String description() default "";

    /**
     * @return if {@code true} a Map or Table will be converted to a TabularData with a CompositeData entry,
     *         if {@code false} the Map or Table will be returned directly.
     */
    boolean convertToTabularData() default true;
}
