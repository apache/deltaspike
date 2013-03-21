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
package org.apache.deltaspike.core.spi.config;

import java.util.Map;

/**
 * <p>Implement this interfaces to provide a ConfigSource.
 * A ConfigSource provides properties from a specific place, like
 * JNDI configuration, a properties file, etc</p>
 * 
 * <p>The custom implementation can be 'registered' using a
 * {@link ConfigSourceProvider} or via the
 * {@link java.util.ServiceLoader} mechanism. In the later case
 * it must get registered via creating a
 * <i>META-INF/services/org.apache.deltaspike.core.spi.config.ConfigSource</i>
 * file and adding the fully qualified class name of your ConfigSource
 * implementation into it. </p>
 */
public interface ConfigSource
{
    /**
     * The default name for the ordinal field.
     * Any ConfigSource might use it's own though or even return a hardcoded
     * in {@link #getOrdinal()}.
     */
    String DELTASPIKE_ORDINAL = "deltaspike_ordinal";
    
    /**
     * Lookup order:
     *
     * <ol>
     *     <li>System properties (ordinal 400)</li>
     *     <li>Environment properties (ordinal 300)</li>
     *     <li>JNDI values (ordinal 200)</li>
     *     <li>Properties file values (/META-INF/apache-deltaspike.properties) (ordinal 100)</li>
     * </ol>
     * <p/>
     * <p><b>Important Hints for custom implementations</b>:</p>
     * <p>
     * If a custom implementation should be invoked <b>before</b> the default implementations, use a value &gt; 400
     * </p>
     * <p>
     * If a custom implementation should be invoked <b>after</b> the default implementations, use a value &lt; 100
     * </p>
     * <p>
     *
     *     <b>IMPORTANT: </b> Have a look at the abstract base-implementation DeltaSpike is using internally,
     *     if a custom implementation should load the ordinal value from the config-source like the default
     *     implementations provided by DeltaSpike do.
     *
     * </p>
     * <p/>
     * <p>Reordering of the default order of the config-sources:</p>
     * <p>Example: If the properties file/s should be used <b>before</b> the other implementations,
     * you have to configure an ordinal &gt; 400. That means, you have to add e.g. deltaspike_ordinal=401 to
     * /META-INF/apache-deltaspike.properties . Hint: In case of property files every file is handled as independent
     * config-source, but all of them have ordinal 400 by default (and can be reordered in a fine-grained manner.</p>
     *
     * @return the 'importance' aka ordinal of the configured values. The higher, the more important.
     */
    int getOrdinal();

    /**
     * Return properties contained in this config source.
     * @return Properties available in this config source.
     */
    Map<String, String> getProperties();

    /**
     * @param key for the property
     * @return configured value or <code>null</code> if this ConfigSource doesn't provide any value for the given key.
     */
    String getPropertyValue(String key);

    /**
     * @return the 'name' of the configuration source, e.g. 'property-file mylocation/myproperty.properties'
     */
    String getConfigName();

}
