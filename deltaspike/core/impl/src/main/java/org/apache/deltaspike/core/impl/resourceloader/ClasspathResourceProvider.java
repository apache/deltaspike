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

import org.apache.deltaspike.core.api.resoureloader.ExternalResource;
import org.apache.deltaspike.core.spi.resourceloader.ExternalResourceProvider;
import org.apache.deltaspike.core.util.ClassUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.InjectionPoint;
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
public class ClasspathResourceProvider implements ExternalResourceProvider
{
    private static final Logger logger = Logger.getLogger(ClasspathResourceProvider.class.getCanonicalName());

    @Override
    public InputStream readStream(final ExternalResource externalResource, final InjectionPoint injectionPoint)
    {
        try
        {
            return readClassPath(externalResource.value());
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

    @Override
    public int getPriority()
    {
        return 10;
    }

    private InputStream readClassPath(final String name) throws IOException
    {
        Enumeration<URL> urls = ClassUtils.getClassLoader(null).getResources(name);
        if (logger.isLoggable(Level.FINEST))
        {
            logger.finest("Found URLS " + urls);
        }
        while (urls.hasMoreElements())
        {
            URL url = urls.nextElement();
            InputStream is = url.openStream();
            if (is != null)
            {
                return is;
            }
        }
        return null;
    }
}
