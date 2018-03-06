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

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Typed;

import org.apache.deltaspike.core.impl.util.JndiUtils;

/**
 * {@link org.apache.deltaspike.core.spi.config.ConfigSource}
 * which uses JNDI for the lookup
 */
@Typed()
class LocalJndiConfigSource extends BaseConfigSource
{
    private static final String BASE_NAME = "java:comp/env/deltaspike/";

    LocalJndiConfigSource()
    {
        initOrdinal(200);
    }

    /**
     * The given key gets used for a lookup via JNDI
     *
     * @param key for the property
     * @return value for the given key or null if there is no configured value
     */
    @Override
    public String getPropertyValue(String key)
    {
        try
        {
            return JndiUtils.lookup(getJndiKey(key), String.class);
        }
        catch (Exception e)
        {
            //do nothing it was just a try
        }
        return null;
    }

    private String getJndiKey(String key)
    {
        if (key.startsWith("java:comp/env"))
        {
            return key;
        }
        return BASE_NAME + key;
    }

    @Override
    public Map<String, String> getProperties()
    {
        Map<String, String> result = new HashMap<String, String>();
        result.putAll(JndiUtils.list(BASE_NAME, String.class));
        result.putAll(JndiUtils.list("java:comp/env", String.class));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigName()
    {
        return BASE_NAME;
    }

    @Override
    public boolean isScannable()
    {
        return false;
    }
}
