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
     * The entry point to the builder-based optionally typed configuration resolution mechanism.
     *
     * String is the default type for configuration entries and is not considered a 'type' by this resolver. Therefore
     * an UntypedResolver is returned by this method. To convert the configuration value to another type, call
     * {@link ConfigResolver.UntypedResolver#as(Class)}.
     *
     * @param name The property key to resolve
     * @return A builder for configuration resolution.
     */
    ConfigResolver.UntypedResolver<String> resolve(String name);

    /**
     * <p>This method can be used to access multiple
     * {@link ConfigResolver.TypedResolver} which must be consistent.
     * The returned {@link ConfigSnapshot} is an immutable object which contains all the
     * resolved values at the time of calling this method.
     *
     * <p>An example would be to access some {@code 'myapp.host'} and {@code 'myapp.port'}:
     * The underlying values are {@code 'oldserver'} and {@code '8080'}.
     *
     * <pre>
     *     // get the current host value
     *     TypedResolver&lt;String&gt; hostCfg config.resolve("myapp.host")
     *              .cacheFor(TimeUnit.MINUTES, 60);
     *
     *     // and right inbetween the underlying values get changed to 'newserver' and port 8082
     *
     *     // get the current port for the host
     *     TypedResolver&lt;Integer&gt; portCfg config.resolve("myapp.port")
     *              .cacheFor(TimeUnit.MINUTES, 60);
     * </pre>
     *
     * In ths above code we would get the combination of {@code 'oldserver'} but with the new port {@code 8081}.
     * And this will obviously blow up because that host+port combination doesn't exist.
     *
     * To consistently access n different config values we can start a {@link ConfigSnapshot} for those values.
     *
     * <pre>
     *     ConfigSnapshot cfgSnap = config.createSnapshot(hostCfg, portCfg);
     *
     *     String host = cfgSnap.getValue(hostCfg);
     *     Integer port = cfgSnap.getValue(portCfg);
     * </pre>
     *
     * Note that there is no <em>close</em> on the snapshot.
     * They should be used as local variables inside a method.
     * Values will not be reloaded for an open {@link ConfigSnapshot}.
     *
     * @param typedResolvers the list of {@link ConfigResolver.TypedResolver} to be accessed in an atomic way
     *
     * @return a new {@link ConfigSnapshot} which holds the resolved values of all the {@param typedResolvers}.
     */
    ConfigSnapshot snapshotFor(ConfigResolver.TypedResolver<?>... typedResolvers);

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
