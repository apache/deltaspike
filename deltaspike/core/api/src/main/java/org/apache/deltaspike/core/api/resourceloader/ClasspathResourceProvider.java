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
package org.apache.deltaspike.core.api.resourceloader;

import org.apache.deltaspike.core.util.ClassUtils;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A classpath-based resource provider.
 */
@ApplicationScoped
public class ClasspathResourceProvider extends AbstractResourceProvider
{
    private static final Logger logger = Logger.getLogger(ClasspathResourceProvider.class.getName());

    @Override
    public InputStream readStream(final InjectableResource injectableResource)
    {
        try
        {
            List<InputStream> matchedStreams = this.readClassPath(injectableResource.location(),true);
            return matchedStreams.get(0);
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
    public List<InputStream> readStreams(InjectableResource injectableResource)
    {
        try
        {
            return readClassPath(injectableResource.location(),false);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Error while trying to load resources from classpath ",e);
        }
    }

    /**
     * Reads all possibly matching classpath entries for the given name.
     *
     * If requireUnique is true, then validates that 1 element is present before returning
     *
     * @param name
     * @param requireUnique
     * @return
     * @throws IOException
     * @throws IllegalStateException
     */
    private List<InputStream> readClassPath(final String name, final boolean requireUnique)
        throws IllegalStateException,IOException
    {
        Enumeration<URL> urls = ClassUtils.getClassLoader(null).getResources(name);
        List<URL> urlList = new ArrayList<URL>();
        List<InputStream> results = new ArrayList<InputStream>();
        while (urls.hasMoreElements())
        {
            URL url = urls.nextElement();
            InputStream is = url.openStream();
            if (is != null)
            {
                results.add(is);
                urlList.add(url);
            }
        }
        if (requireUnique && results.size() != 1)
        {
            String msg = urlsToString(urlList,name);
            for (InputStream is : results)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    if (logger.isLoggable(Level.FINE))
                    {
                        logger.log(Level.FINE,"Unable to close stream",e);
                    }
                }
            }
            throw new IllegalStateException(msg);
        }
        return results;
    }

    private String urlsToString(List<URL> urls, String name)
    {
        if (urls.isEmpty())
        {
            return String.format("No resources found for '%s'",name);
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("multiple resources found for '%s'",name));
            for (URL u : urls)
            {
                sb.append(" Match : ").append(u.toExternalForm());
            }
            return sb.toString();
        }
    }
}
