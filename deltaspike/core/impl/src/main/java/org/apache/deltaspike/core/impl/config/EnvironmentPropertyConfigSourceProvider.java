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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;
import org.apache.deltaspike.core.util.PropertyFileUtils;

/**
 * Register all property files with the given propertyFileName
 * as {@link ConfigSource}.
 */
public class EnvironmentPropertyConfigSourceProvider implements ConfigSourceProvider
{
    private List<ConfigSource> configSources = new ArrayList<ConfigSource>();

    public EnvironmentPropertyConfigSourceProvider(String propertyFileName)
    {
        try
        {
            Enumeration<URL> propertyFileUrls = PropertyFileUtils.resolvePropertyFiles(propertyFileName);

            while (propertyFileUrls.hasMoreElements())
            {
                URL propertyFileUrl = propertyFileUrls.nextElement();
                configSources.add(new PropertyFileConfigSource(propertyFileUrl));
            }
        }
        catch (IOException ioe)
        {
            throw new IllegalStateException("problem while loading DeltaSpike property files", ioe);
        }

    }

    @Override
    public List<ConfigSource> getConfigSources()
    {
        return configSources;
    }
}
