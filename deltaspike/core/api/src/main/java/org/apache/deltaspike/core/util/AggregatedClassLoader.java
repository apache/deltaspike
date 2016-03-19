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

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AggregatedClassLoader extends ClassLoader
{

    private final List<ClassLoader> classLoaders;

    public AggregatedClassLoader(List<ClassLoader> classLoaders)
    {
        super();
        this.classLoaders = classLoaders;
    }

    public static AggregatedClassLoader newInstance()
    {
        return new AggregatedClassLoader(Arrays.asList(
                AggregatedClassLoader.class.getClassLoader(),
                Thread.currentThread().getContextClassLoader(),
                ClassLoader.getSystemClassLoader()));
    }

    @Override
    public URL getResource(String name)
    {
        for (ClassLoader loader : classLoaders)
        {
            URL url = loader.getResource(name);
            if (url != null)
            {
                return url;
            }
        }
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException
    {
        final Set<URL> result = new LinkedHashSet<URL>();
        
        for (ClassLoader loader : classLoaders)
        {
            Enumeration<URL> urls = loader.getResources(name);
            while (urls.hasMoreElements())
            {
                result.add(urls.nextElement());
            }
        }
        
        return new Enumeration<URL>()
        {
            private final Iterator<URL> iterator = result.iterator();

            @Override
            public URL nextElement()
            {
                return iterator.next();
            }

            @Override
            public boolean hasMoreElements()
            {
                return iterator.hasNext();
            }
        };
    }

}
