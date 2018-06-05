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
package org.apache.deltaspike.core.impl.config;

import org.apache.deltaspike.core.api.config.ConfigResolver;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ConfigHelperImpl implements ConfigResolver.ConfigHelper
{

    @Override
    public Set<String> diffConfig(Map<String, String> oldValues, Map<String, String> newValues)
    {
        if (oldValues == null)
        {
            oldValues = Collections.emptyMap();
        }
        if (newValues == null)
        {
            newValues = Collections.emptyMap();
        }
        Set<String> changedAttribs = new HashSet<>();
        Set<String> oldKeys = new HashSet<>(oldValues.keySet());
        for (Map.Entry<String, String> newPropEntry : newValues.entrySet())
        {
            String key = newPropEntry.getKey();
            if (oldValues.containsKey(key))
            {
                if (compare(oldValues.get(key), newPropEntry.getValue()) != 0)
                {
                    changedAttribs.add(key);
                }
                oldKeys.remove(key);
            }
            else
            {
                changedAttribs.add(key);
            }
        }
        changedAttribs.addAll(oldKeys);

        return changedAttribs;
    }

    private int compare(String a, String b)
    {
        if (a == null && b == null)
        {
            return 0;
        }
        if (a != null)
        {
            return a.compareTo(b);
        }
        return 1;
    }
}
