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

/**
 * Utilities for working with property queries
 *
 * @see PropertyQuery
 */
public class PropertyQueries
{

    private PropertyQueries()
    {
    }

    /**
     * Create a new {@link PropertyQuery}
     *
     * @param <V>
     * @param targetClass
     * @return
     */
    public static <V> PropertyQuery<V> createQuery(Class<?> targetClass)
    {
        return new PropertyQuery<V>(targetClass);
    }

}
