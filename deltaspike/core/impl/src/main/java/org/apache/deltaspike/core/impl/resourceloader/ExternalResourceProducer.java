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

import org.apache.deltaspike.core.api.resourceloader.ExternalResource;
import org.apache.deltaspike.core.api.resourceloader.ExternalResourceStorage;
import org.apache.deltaspike.core.spi.literal.StorageTypeLiteral;
import org.apache.deltaspike.core.spi.resourceloader.ExternalResourceProvider;
import org.apache.deltaspike.core.spi.resourceloader.StorageType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Handles the creation/loading of external resources.
 *
 */
@ApplicationScoped
public class ExternalResourceProducer
{
    private static final Logger logger = Logger.getLogger(ExternalResourceProducer.class.getName());

    @Inject
    @Any
    private Instance<ExternalResourceProvider> resourceProviders;

    private ExternalResourceProvider getProvider(Class<? extends ExternalResourceStorage> storageTypeClass)
    {
        StorageType storageType = new StorageTypeLiteral(storageTypeClass);
        ExternalResourceProvider provider = resourceProviders.select(storageType).get();
        return provider;
    }

    @Produces
    @ExternalResource(storage = ExternalResourceStorage.class,location = "")
    public InputStream getInputStream(final InjectionPoint injectionPoint)
    {
        ExternalResource externalResource = getAnnotation(injectionPoint);
        ExternalResourceProvider provider = getProvider(externalResource.storage());
        final InputStream is = provider.readStream(externalResource);
        return is;
    }

    @Produces
    @ExternalResource(storage = ExternalResourceStorage.class,location = "")
    public Properties getProperties(final InjectionPoint injectionPoint) throws IOException
    {
        ExternalResource externalResource = getAnnotation(injectionPoint);
        ExternalResourceProvider provider = getProvider(externalResource.storage());
        final Properties properties = provider.readProperties(externalResource);
        return properties;
    }

    public void closeInputStream(@Disposes @ExternalResource(storage = ExternalResourceStorage.class,location = "")
                                 InputStream inputStream)
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
        System.out.println("processing injection point " + injectionPoint +
                " with qualifiers " + injectionPoint.getQualifiers());
        for (Annotation annotation : injectionPoint.getQualifiers())
        {
            if (annotation instanceof ExternalResource)
            {
                return (ExternalResource)annotation;
            }
        }
        return null;
    }

}
