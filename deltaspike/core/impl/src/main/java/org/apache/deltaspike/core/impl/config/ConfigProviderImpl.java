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

import org.apache.deltaspike.core.api.config.Config;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.util.ClassUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class ConfigProviderImpl implements ConfigResolver.ConfigProvider
{
    /**
     * The content of this map will get lazily initiated and will hold the
     * Configs for each WebApp/EAR, etc (thus the ClassLoader).
     */
    private static Map<ClassLoader, ConfigImpl> configs = new ConcurrentHashMap<>();

    @Override
    public Config getConfig()
    {
        ClassLoader cl = ClassUtils.getClassLoader(null);
        return getConfig(cl);
    }

    @Override
    public Config getConfig(ClassLoader cl)
    {
        ConfigImpl config = configs.get(cl);
        if (config == null)
        {
            config = new ConfigImpl(cl);
            config.init();
            ConfigImpl oldConfig = configs.put(cl, config);
            if (oldConfig != null)
            {
                config = oldConfig;
            }
        }
        return config;
    }

    @Override
    public void releaseConfig(ClassLoader cl)
    {
        ConfigImpl oldConfig = configs.remove(cl);
        if (oldConfig != null)
        {
            oldConfig.release();
        }

        // And remove all the children as well.
        // This will e.g happen in EAR scenarios
        Iterator<Map.Entry<ClassLoader, ConfigImpl>> it = configs.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<ClassLoader, ConfigImpl> cfgEntry = it.next();
            if (isChildClassLoader(cl, cfgEntry.getKey()))
            {
                cfgEntry.getValue().release();
                it.remove();
            }
        }
    }

    @Override
    public ConfigResolver.ConfigHelper getHelper()
    {
        return new ConfigHelperImpl();
    }

    private boolean isChildClassLoader(ClassLoader configClassLoader, ClassLoader suspect)
    {
        ClassLoader suspectParentCl = suspect.getParent();
        if (suspectParentCl == null)
        {
            return false;
        }

        if (suspectParentCl == configClassLoader)
        {
            return true;
        }

        return isChildClassLoader(configClassLoader, suspectParentCl);
    }
}
