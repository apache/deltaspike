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
package org.apache.deltaspike.jsf.impl.util;

import java.util.Arrays;
import java.util.List;

public class RequestParameter
{
    private final String key;
    private final String[] values;

    /**
     * Constructor for creating a parameter for the given key and values
     * @param key current key
     * @param values current values
     */
    RequestParameter(String key, String[] values)
    {
        this.key = key;
        this.values = values;
    }

    /**
     * Key of the parameter
     * @return current key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Exposes the values of the parameter as list
     * @return values of the parameter
     */
    public List<String> getValueList()
    {
        return Arrays.asList(this.values);
    }

    /**
     * Exposes the values of the parameter as array
     * @return values of the parameter
     */
    public String[] getValues()
    {
        return values;
    }
}
