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
package org.apache.deltaspike.core.impl.resourceLoader;

import org.apache.deltaspike.core.api.resoureLoader.ExternalResource;
import org.apache.deltaspike.core.api.resoureLoader.XMLProperties;
import org.apache.deltaspike.core.spi.resourceLoader.ExternalResourceProvider;
import org.apache.deltaspike.core.util.BeanUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Handles the creation/loading of external resources.
 *
 */
@ApplicationScoped
public class ExternalResourceProducer
{
    private static final Logger logger = Logger.getLogger(ExternalResourceProducer.class.getCanonicalName());

    @Inject
    @Any
    private Instance<ExternalResourceProvider> resourceProviders;

    @Produces
    @ExternalResource("")
    public InputStream getInputStream(final InjectionPoint injectionPoint)
    {
        final InputStream is = findInputStream(injectionPoint);
        return is;
    }

    @Produces
    @ExternalResource("")
    public Properties getProperties(final InjectionPoint injectionPoint) throws IOException
    {
        final InputStream is = findInputStream(injectionPoint);
        final boolean isXml = BeanUtils.extractAnnotation(injectionPoint.getAnnotated(), XMLProperties.class) != null;
        if (is != null)
        {
            Properties properties = new Properties();
            if (isXml)
            {
                properties.loadFromXML(is);
            }
            else
            {
                properties.load(is);
            }
            is.close();
            return properties;
        }
        else
        {
            return null;
        }
    }

    public void closeInputStream(@Disposes @ExternalResource("") InputStream inputStream)
    {
        if (inputStream != null)
        {
            try
            {
                inputStream.close();
            }
            catch (IOException e)
            {

            }
        }
    }

    private ExternalResource getAnnotation(final InjectionPoint injectionPoint)
    {
        return BeanUtils.extractAnnotation(injectionPoint.getAnnotated(),ExternalResource.class);
    }

    private InputStream findInputStream(final InjectionPoint injectionPoint)
    {
        final ExternalResource externalResource = getAnnotation(injectionPoint);
        final List<ExternalResourceProvider> providerList = new ArrayList<ExternalResourceProvider>();
        for (ExternalResourceProvider erp : resourceProviders)
        {
            providerList.add(erp);
        }
        Collections.sort(providerList,new ExternalResourceProviderComparator());
        for (final ExternalResourceProvider provider : providerList)
        {
            final InputStream is = provider.readStream(externalResource,injectionPoint);
            if (is != null)
            {
                return is;
            }
        }
        return null;
    }

}
