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
package org.apache.deltaspike.core.impl.resourceloader;

import org.apache.deltaspike.core.api.resourceloader.ClasspathStorage;
import org.apache.deltaspike.core.api.resourceloader.ExternalResource;
import org.apache.deltaspike.core.spi.resourceloader.StorageType;
import org.apache.deltaspike.core.util.ClassUtils;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A classpath based resource provider
 */
@ApplicationScoped
@StorageType(ClasspathStorage.class)
public class ClasspathResourceProvider extends BaseResourceProvider
{
    private static final Logger logger = Logger.getLogger(ClasspathResourceProvider.class.getName());

    @Override
    public InputStream readStream(final ExternalResource externalResource)
    {
        try
        {
            return readClassPath(externalResource.location());
        }
        catch (IOException e)
        {
            if (logger.isLoggable(Level.FINE))
            {
                logger.log(Level.FINE, "Problem reading resource.", e);
            }
            return null;
        }
    }

    private InputStream readClassPath(final String name) throws IOException
    {
        Enumeration<URL> urls = ClassUtils.getClassLoader(null).getResources(name);

        InputStream result = null;
        URL firstURL = null;
        while (urls.hasMoreElements())
        {
            URL url = urls.nextElement();
            InputStream is = url.openStream();
            if (is != null)
            {
                if (firstURL != null)
                {
                    try
                    {
                        result.close();
                    }
                    finally
                    {
                        is.close();
                    }
                    throw new IllegalStateException("multiple files found for '" + name +
                        "' (" + firstURL.toExternalForm() + ", " + url.toExternalForm() + ")");
                }
                firstURL = url;
                result = is;
            }
        }
        return result;
    }
}
