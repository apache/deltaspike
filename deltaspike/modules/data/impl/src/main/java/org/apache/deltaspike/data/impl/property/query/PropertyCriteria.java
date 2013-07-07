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
package org.apache.deltaspike.data.impl.property.query;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * <p>
 * A property criteria can be used to filter the properties found by a {@link PropertyQuery}
 * </p>
 * <p/>
 * <p>
 * Solder provides a number of property queries ( {@link TypedPropertyCriteria}, {@link NamedPropertyCriteria} and
 * {@link AnnotatedPropertyCriteria}), or you can create a custom query by implementing this interface.
 * </p>
 *
 * @see PropertyQuery#addCriteria(PropertyCriteria)
 * @see PropertyQueries
 * @see TypedPropertyCriteria
 * @see AnnotatedPropertyCriteria
 * @see NamedPropertyCriteria
 */
public interface PropertyCriteria
{
    /**
     * Tests whether the specified field matches the criteria
     *
     * @param f
     * @return true if the field matches
     */
    boolean fieldMatches(Field f);

    /**
     * Tests whether the specified method matches the criteria
     *
     * @param m
     * @return true if the method matches
     */
    boolean methodMatches(Method m);
}
