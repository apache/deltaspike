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

import java.util.logging.Level;
import java.util.logging.Logger;

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
public abstract class ConfigSource
{
    protected Logger LOG = Logger.getLogger(getClass().getName());

    //X TODO discuss value
    private static final String ORDINAL_KEY = "org_apache_deltaspike_ORDINAL";

    private int ordinal;

    protected ConfigSource()
    {
        init();
    }

    /**
     * @return the 'importance' aka ordinal of the configured values. The higher, the more important.
     */
    public int getOrdinal()
    {
        return this.ordinal;
    }

    /**
     * @param key for the property
     * @return configured value or <code>null</code> if this ConfigSource doesn't provide any value for the given key.
     */
    public abstract String getPropertyValue(String key);

    /**
     * @return the 'name' of the configuration source, e.g. 'property-file mylocation/myproperty.properties'
     */
    public abstract String getConfigName();

    /**
     * Provides the default ordinal, if there isn't a custom ordinal for the current
     * {@link ConfigSource}
     * @return value for the default ordinal
     */
    protected int getDefaultOrdinal()
    {
        return 1000;
    }

    /**
     * Init method e.g. for initializing the ordinal
     */
    protected void init()
    {
        this.ordinal = getDefaultOrdinal();

        Integer configuredOrdinal = null;

        String configuredOrdinalString = getPropertyValue(getOrdinalKey());
        try
        {
            if(configuredOrdinalString != null)
            {
                configuredOrdinal = Integer.valueOf(configuredOrdinalString.trim());
            }
        }
        catch (NumberFormatException e)
        {
            LOG.log(Level.WARNING,
                    "The configured config-ordinal isn't a valid integer. Invalid value: " + configuredOrdinalString);
        }
        catch (Exception e)
        {
            //do nothing it was just a try
        }

        if(configuredOrdinal != null)
        {
            this.ordinal = configuredOrdinal;
        }
    }

    /**
     * Allows to customize the key which gets used to lookup a customized ordinal for the current
     * {@link ConfigSource}
     * @return key which should be used for the ordinal lookup
     */
    protected String getOrdinalKey()
    {
        return ORDINAL_KEY;
    }
}
