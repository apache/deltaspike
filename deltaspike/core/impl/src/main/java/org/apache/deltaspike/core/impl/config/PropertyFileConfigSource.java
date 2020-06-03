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

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.util.PropertyFileUtils;

/**
 * {@link org.apache.deltaspike.core.spi.config.ConfigSource} which uses
 * a fixed property file for the lookup.
 *
 * If the property file has a 'file://' protocol, we are able to pick up
 * changes during runtime when the underlying property file changes.
 * This does not make sense for property files in JARs, but makes perfect sense
 * whenever a property file URL is directly on the file system.
 */
public class PropertyFileConfigSource extends BaseConfigSource
{
    /**
     * The name of a property which can be defined inside the property file
     * to define the amount of seconds after which the property file should
     * be tested for changes again.
     * Note that the test is performed by storing the lastChanged attribute of the
     * underlying file.
     *
     * By default the time after which we look for changes is {@link #RELOAD_PERIOD_DEFAULT}.
     * This can be changed by explicitly adding a property with the name defined in {@link #RELOAD_PERIOD}
     * which contains the number of seconds after which we try to reload again.
     * A zero or negative value means no dynamic reloading.
     * <pre>
     * # look for changes after 60 seconds
     * deltaspike_reload=60
     * </pre>
     * Whether the file got changed is determined by the lastModifiedDate of the underlying file.
     * <p>
     * You can disable the whole reloading with a negative reload time, e.g.
     * <pre>
     * deltaspike_reload=-1
     * </pre>
     */
    public static final String RELOAD_PERIOD = "deltaspike_reload";
    public static final int RELOAD_PERIOD_DEFAULT = 300;

    private final ConfigResolver.ConfigHelper configHelper;

    /**
     * currently loaded config properties.
     */
    private Map<String, String> properties;

    private final URL propertyFileUrl;
    private String filePath;

    private int reloadAllSeconds = RELOAD_PERIOD_DEFAULT;
    private Instant fileLastModified = null;

    /**
     * Reload after that time in seconds.
     */
    private int reloadAfterSec;

    private Consumer<Set<String>> reportAttributeChange;

    public PropertyFileConfigSource(URL propertyFileUrl)
    {
        this.propertyFileUrl = propertyFileUrl;
        filePath = propertyFileUrl.toExternalForm();

        this.properties = toMap(PropertyFileUtils.loadProperties(propertyFileUrl));

        if (isFile(propertyFileUrl))
        {

            calculateReloadTime();
            if (reloadAllSeconds < 0 )
            {
                configHelper = null;
            }
            else
            {
                fileLastModified = getLastModified();
                configHelper = ConfigResolver.getConfigProvider().getHelper();
                reloadAfterSec = getNowSeconds() + reloadAllSeconds;
            }
        }
        else
        {
            configHelper = null;
        }

        initOrdinal(100);
    }

    private void calculateReloadTime()
    {
        final String reloadPeriod = properties.get(RELOAD_PERIOD);
        if (reloadPeriod != null)
        {
            try
            {
                int reload = Integer.parseInt(reloadPeriod);
                if (reload < 0)
                {
                    fileLastModified = null;
                    log.info("Disable dynamic reloading for ConfigSource " + filePath);
                }
                else
                {
                    reloadAllSeconds = reload;
                }
            }
            catch (NumberFormatException nfe)
            {
                log.warning("Wrong value for " + RELOAD_PERIOD + " property: " + reloadPeriod +
                    ". Must be numeric in seconds. Using default " + RELOAD_PERIOD_DEFAULT);
                reloadAllSeconds = RELOAD_PERIOD_DEFAULT;
            }
        }
    }

    protected Map<String, String> toMap(Properties properties)
    {
        Map<String,String> result = new HashMap<>(properties.size());
        for (String propertyName : properties.stringPropertyNames())
        {
            result.put(propertyName, properties.getProperty(propertyName));
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<String, String> getProperties()
    {
        if (needsReload())
        {
            reloadProperties();
        }

        return properties;
    }

    @Override
    public String getPropertyValue(String key)
    {
        if (needsReload())
        {
            reloadProperties();
        }

        return properties.get(key);
    }

    private boolean needsReload()
    {
        if (fileLastModified != null && getNowSeconds() > reloadAfterSec)
        {
            final Instant newLastModified = getLastModified();
            if (newLastModified != null && newLastModified.isAfter(fileLastModified))
            {
                return true;
            }
        }

        return false;
    }

    private synchronized void reloadProperties()
    {
        // another thread might have already updated the properties.
        if (needsReload())
        {
            final Map<String, String> newProps = toMap(PropertyFileUtils.loadProperties(propertyFileUrl));

            final Set<String> modfiedAttributes = configHelper.diffConfig(properties, newProps);
            if (!modfiedAttributes.isEmpty())
            {
                reportAttributeChange.accept(modfiedAttributes);
            }

            this.properties = newProps;

            fileLastModified = getLastModified();

            calculateReloadTime();
            reloadAfterSec = getNowSeconds() + reloadAllSeconds;
        }
    }

    private int getNowSeconds()
    {
        // this might overrun all 100 years or so.
        // I think we can live with a faster reload all 100 years
        // if we can spare needing to deal with atomic updates ;)
        return (int) TimeUnit.NANOSECONDS.toSeconds(System.nanoTime());
    }

    private Instant getLastModified()
    {
        try
        {
            return Files.getLastModifiedTime(Paths.get(propertyFileUrl.toURI())).toInstant();
        }
        catch (Exception e)
        {
            log.log(Level.WARNING,
                "Cannot dynamically reload property file {0}. Not able to read last modified date", filePath);
            return null;
        }
    }

    private boolean isFile(URL propertyFileUrl)
    {
        return "file".equalsIgnoreCase(propertyFileUrl.getProtocol());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigName()
    {
        return filePath;
    }

    @Override
    public void setOnAttributeChange(Consumer<Set<String>> reportAttributeChange)
    {
        this.reportAttributeChange = reportAttributeChange;
    }

    @Override
    public boolean isScannable()
    {
        return true;
    }
}
