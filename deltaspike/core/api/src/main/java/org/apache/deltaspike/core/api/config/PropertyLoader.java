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

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ProjectStageProducer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to load configuration properties via arbitrary property files in a well defined order.
 *
 * <p>
 * This will also pick up property files with names suffixed with {@code -<project stage>}, e.g.
 * myconfig-Production.properties.</p>
 * <p>
 * User configurations should have {@code deltaspike_ordinal} as the first property, with a value greater than
 * 100.</p>
 *
 */
public class PropertyLoader
{
    public static final int CONFIGURATION_ORDINAL_DEFAULT_VALUE = 100;


    private static final String FILE_EXTENSION = ".properties";


    private static final Logger LOG = Logger.getLogger(PropertyLoader.class.getName());


    private PropertyLoader()
    {
        // utility class doesn't have a public ct
    }

    /**
     * Looks for all properties files with the given name in the classpath, loads them in ascending order determined by
     * their ordinal and merges them.
     *
     * <p>
     * The idea is to be able to override properties by just providing a new properties file with the same name but a
     * higher 'deltaspike_ordinal' than the old one.</p>
     *
     * <p>
     * If a property file defines no 'deltaspike_ordinal' property than a default value of
     * {@link #CONFIGURATION_ORDINAL_DEFAULT_VALUE} is assumed. Any sensitive default which is provided by the system
     * parsing for the configuration should have a 'deltaspike_ordinal' value lower than 10. In most cases a value of
     * 1.</p>
     *
     * <p>
     * If two property files have the same 'deltaspike_ordinal', their order is undefined. The Properties file which
     * gets found first will be processed first and thus gets overwritten by the one found later.</p>
     *
     * @param propertyFileName the name of the properties file, without the extension '.properties'
     *
     * @return the final property values
     */
    public static synchronized Properties getProperties(String propertyFileName)
    {
        if (propertyFileName == null)
        {
            throw new IllegalArgumentException("propertyFileName must not be null!");
        }

        try
        {
            if (propertyFileName.endsWith(FILE_EXTENSION))
            {
                // if the given propertyFileName already contains the extension, then remove it.
                propertyFileName = propertyFileName.substring(0, propertyFileName.length() - FILE_EXTENSION.length());
            }

            List<Properties> allProperties = loadAllProperties(propertyFileName);
            if (allProperties == null)
            {
                return null;
            }
            
            List<Properties> sortedProperties = sortProperties(allProperties);
            Properties properties = mergeProperties(sortedProperties);
            return properties;
        }
        catch (IOException e)
        {
            LOG.log(Level.SEVERE, "Error while loading the propertyFile " + propertyFileName, e);
            return null;
        }
    }

    private static List<Properties> loadAllProperties(String propertyFileName)
        throws IOException
    {
        ClassLoader cl = ClassUtils.getClassLoader(null);

        List<Properties> properties = new ArrayList<Properties>();

        // read the normal property file names
        Enumeration<URL> propertyUrls = cl.getResources(propertyFileName + FILE_EXTENSION);
        while (propertyUrls != null && propertyUrls.hasMoreElements())
        {
            URL propertyUrl = propertyUrls.nextElement();
            fillProperties(properties, propertyUrl);
        }

        // and also read the ones post-fixed with the projectStage
        ProjectStage ps = ProjectStageProducer.getInstance().getProjectStage();

        propertyUrls = cl.getResources(propertyFileName + "-" + ps + FILE_EXTENSION);
        while (propertyUrls != null && propertyUrls.hasMoreElements())
        {
            URL propertyUrl = propertyUrls.nextElement();
            fillProperties(properties, propertyUrl);
        }

        if (properties.isEmpty())
        {
            if (LOG.isLoggable(Level.INFO))
            {
                LOG.info("could not find any property files with name " + propertyFileName);
            }

            return null;
        }

        return properties;
    }

    private static void fillProperties(List<Properties> properties, URL propertyUrl) throws IOException
    {
        InputStream is = null;
        try
        {
            is = propertyUrl.openStream();
            Properties prop = new Properties();
            prop.load(is);
            properties.add(prop);

            // a bit debugging output
            int ordinal = getConfigurationOrdinal(prop);
            if (LOG.isLoggable(Level.FINE))
            {
                LOG.fine("loading properties with ordinal " + ordinal + " from file " + propertyUrl.getFile());
            }
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }

    /**
     * Implement a quick and dirty sorting mechanism for the given Properties.
     * @param allProperties
     * @return the Properties list sorted by it's 'configuration.ordinal' in ascending order.
     */
    private static List<Properties> sortProperties(List<Properties> allProperties)
    {
        List<Properties> sortedProperties = new ArrayList<Properties>();
        for (Properties p : allProperties)
        {
            int configOrder = getConfigurationOrdinal(p);

            int i;
            for (i = 0; i < sortedProperties.size(); i++)
            {
                int listConfigOrder = getConfigurationOrdinal(sortedProperties.get(i));
                if (listConfigOrder > configOrder)
                {
                    // only go as far as we found a higher priority Properties file
                    break;
                }
            }
            sortedProperties.add(i, p);
        }
        return sortedProperties;
    }

    /**
     * Determine the 'deltaspike_ordinal' of the given properties.
     * {@link #CONFIGURATION_ORDINAL_DEFAULT_VALUE} if
     * {@link ConfigSource#DELTASPIKE_ORDINAL} is not set in the
     * Properties file.
     *
     * @param p the Properties from the file.
     * @return the ordinal number of the given Properties file.
     */
    private static int getConfigurationOrdinal(Properties p)
    {
        int configOrder = CONFIGURATION_ORDINAL_DEFAULT_VALUE;

        String configOrderString = p.getProperty(ConfigSource.DELTASPIKE_ORDINAL);
        if (configOrderString != null && configOrderString.length() > 0)
        {
            try
            {
                configOrder = Integer.parseInt(configOrderString);
            }
            catch (NumberFormatException nfe)
            {
                LOG.severe(ConfigSource.DELTASPIKE_ORDINAL + " must be an integer value!");
                throw nfe;
            }
        }

        return configOrder;
    }

    /**
     * Merge the given Properties in order of appearance.
     * @param sortedProperties
     * @return the merged Properties
     */
    private static Properties mergeProperties(List<Properties> sortedProperties)
    {
        Properties mergedProperties = new Properties();
        for (Properties p : sortedProperties)
        {
            for (Map.Entry<?, ?> entry : p.entrySet())
            {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                if (!ConfigSource.DELTASPIKE_ORDINAL.equals(key))
                {
                    // simply overwrite the old properties with the new ones.
                    mergedProperties.setProperty(key, value);
                }
            }
        }

        return mergedProperties;
    }

}
