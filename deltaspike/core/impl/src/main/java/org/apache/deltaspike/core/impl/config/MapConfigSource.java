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

import java.util.Map;

/**
 * Base class for configurations based on regular {@link Map}
 */
public abstract class MapConfigSource extends BaseConfigSource
{

    private final Map<String, String> map;

    // only needed for some old Weld versions
    public MapConfigSource()
    {
        map = null;
    }

    public MapConfigSource(Map<String, String> map)
    {
        this.map = map;
    }

    @Override
    public Map<String, String> getProperties()
    {
        return map;
    }

    @Override
    public String getPropertyValue(String key)
    {
        return map.get(key);
    }

    @Override
    public boolean isScannable()
    {
        return true;
    }

}
