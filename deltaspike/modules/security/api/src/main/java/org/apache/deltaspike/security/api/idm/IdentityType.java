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
package org.apache.deltaspike.security.api.idm;

import java.util.Map;

/**
 * IdentityObject
 *
 */
public interface IdentityType
{

    String getKey();


    // Attributes

    /**
     * Set attribute with given name and value. Operation will overwrite any previous value.
     * Null value will remove attribute.
     *
     * @param name  of attribute
     * @param value to be set
     */
    void setAttribute(String name, String value);

    /**
     * Set attribute with given name and values. Operation will overwrite any previous values.
     * Null value or empty array will remove attribute.
     *
     * @param name   of attribute
     * @param values to be set
     */
    void setAttribute(String name, String[] values);

    /**
     * Remove attribute with given name
     *
     * @param name of attribute
     */
    void removeAttribute(String name);

    /**
     * @param name of attribute
     * @return attribute values or null if attribute with given name doesn't exist. If given attribute has many values
     *         method will return first one
     */
    String getAttribute(String name);

    /**
     * @param name of attribute
     * @return attribute values or null if attribute with given name doesn't exist
     */
    String[] getAttributeValues(String name);

    /**
     * @return map of attribute names and their values
     */
    Map<String, String[]> getAttributes();

}
