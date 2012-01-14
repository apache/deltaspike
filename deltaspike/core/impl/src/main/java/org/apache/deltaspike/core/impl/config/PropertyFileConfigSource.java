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

import org.apache.deltaspike.core.spi.config.ConfigSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * {@link ConfigSource} which uses
 * <i>META-INF/apache-deltaspike.properties</i> for the lookup
 */
class PropertyFileConfigSource implements ConfigSource
{
    private Properties properties;
    private String fileName;

    PropertyFileConfigSource(URL propertyFileUrl)
    {
        fileName = propertyFileUrl.toExternalForm();
        properties = loadProperties(propertyFileUrl);

        String ordinalVal = getPropertyValue(ConfigSource.DELTASPIKE_ORDINAL);
        if (ordinalVal != null && ordinalVal.length() > 0)
        {
            ordinal = Integer.valueOf(ordinalVal);
        }
    }

    private int ordinal = 100;
    
    @Override
    public int getOrdinal()
    {
        return ordinal;
    }

    /**
     * The given key gets used for a lookup via a properties file
     *
     * @param key for the property
     * @return value for the given key or null if there is no configured value
     */
    @Override
    public String getPropertyValue(String key)
    {
        return (String) properties.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigName()
    {
        return fileName;
    }



    private Properties loadProperties(URL url)
    {
        Properties props = new Properties();

        InputStream inputStream = null;
        try
        {
            inputStream = url.openStream();

            if (inputStream != null)
            {
                props.load(inputStream);
            }
        }
        catch (IOException e)
        {
            return null;
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (IOException e)
            {
                // no worries, means that the file is already closed
            }
        }

        return props;
    }
}
