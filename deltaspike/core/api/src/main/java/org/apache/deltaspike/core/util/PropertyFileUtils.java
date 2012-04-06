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
package org.apache.deltaspike.core.util;

import javax.enterprise.inject.Typed;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

@Typed()
public abstract class PropertyFileUtils
{
    /**
     * Constructor which prevents the instantiation of this class
     */
    private PropertyFileUtils()
    {
        // prevent instantiation
    }

    public static Enumeration<URL> resolvePropertyFiles(String propertyFileName) throws IOException
    {
        ClassLoader cl = ClassUtils.getClassLoader(null);

        Enumeration<URL> propertyFileUrls = cl.getResources(propertyFileName);

        //fallback - see DELTASPIKE-98
        if (!propertyFileUrls.hasMoreElements())
        {
            cl = PropertyFileUtils.class.getClassLoader();
            propertyFileUrls = cl.getResources(propertyFileName);
        }

        return propertyFileUrls;
    }

    public static Properties loadProperties(URL url)
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
            throw new IllegalStateException(e);
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

    public static ResourceBundle getResourceBundle(String bundleName)
    {
        return getResourceBundle(bundleName, Locale.getDefault());
    }

    public static ResourceBundle getResourceBundle(String bundleName, Locale locale)
    {
        return ResourceBundle.getBundle(bundleName, locale);
    }
}