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
package org.apache.deltaspike.core.spi;


import java.io.Serializable;

/**
 * This interface is just for internal use.
 * It should ensure that refactorings don't lead to different method-names for the same purpose.
 * Furthermore, it allows to easily find all artifacts which allow generic attributes.
 */
public interface AttributeAware extends Serializable
{
    /**
     * Sets an attribute
     * @param name name of the attribute
     * @param value value of the attribute (null values aren't allowed)
     * @return the old value or <code>null</code> if no previous value did exist
     */
    Object setAttribute(String name, Object value);

    /**
     * Returns true if there is a value for the given name
     * @param name name of the argument in question
     * @return true if there is a value for the given name, false otherwise
     */
    boolean containsAttribute(String name);

    /**
     * Exposes the value for the given name
     * @param name name of the attribute
     * @param targetType type of the attribute
     * @param <T> current type
     * @return value of the attribute, or null if there is no attribute with the given name
     */
    <T> T getAttribute(String name, Class<T> targetType);
}
