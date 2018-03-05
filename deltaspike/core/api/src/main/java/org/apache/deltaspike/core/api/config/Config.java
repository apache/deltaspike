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
package org.apache.deltaspike.core.api.config;

import org.apache.deltaspike.core.spi.config.ConfigFilter;
import org.apache.deltaspike.core.spi.config.ConfigSource;

import java.util.List;

/**
 * The Configuration for an application/ClassLoader.
 */
public interface Config
{

    /**
     * @return all the current ConfigSources for this Config
     */
    ConfigSource[] getConfigSources();

    /**
     * This method can be used for programmatically adding {@link ConfigSource}s.
     * It is not needed for normal 'usage' by end users, but only for Extension Developers!
     *
     * @param configSourcesToAdd the ConfigSources to add
     */
    void addConfigSources(List<ConfigSource> configSourcesToAdd);

    /**
     * @return the {@link ConfigFilter}s for the current application.
     */
    List<ConfigFilter> getConfigFilters();

    /**
     * Add a {@link ConfigFilter} to the ConfigResolver. This will only affect the current WebApp (or more precisely the
     * current ClassLoader and it's children).
     *
     * @param configFilter
     */
    void addConfigFilter(ConfigFilter configFilter);

    /**
     * Filter the configured value.
     * This can e.g. be used for decryption.
     * @param key the key of the config property
     * @param value to be filtered
     * @param forLog whether the value is intended to be presented to some humans somehow.
     *               If filtered for logging, then secrets might get starred out '*****'.
     * @return the filtered value
     */
    String filterConfigValue(String key, String value, boolean forLog);
}
