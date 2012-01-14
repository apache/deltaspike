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

/**
 * <p>Implement this interfaces to provide a ConfigSource.
 * A ConfigSource provides properties from a specific place, like
 * JNDI configuration, a properties file, etc</p>
 * 
 * <p>A ConfigSourceProvider which is not provided via
 * {@link ConfigSourceProvider} will get picked up via the 
 * {@link java.util.ServiceLoader} and therefor must get registered via
 * META-INF/services/org.apache.deltaspike.core.spi.config.ConfigSource</p>
 */
public interface ConfigSource
{
    /**
     * The default name for the ordinal field.
     * Any ConfigSource might use it's own though or even return a hardcoded
     * in {@link #getOrdinal()}.
     */
    static final String DELTASPIKE_ORDINAL = "deltaspike_ordinal";
    
    /**
     * @return the 'importance' aka ordinal of the configured values. The higher, the more important.
     */
    int getOrdinal();

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
