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
import org.apache.deltaspike.core.api.resoureloader.XMLProperties;
import org.apache.deltaspike.core.spi.resourceloader.ExternalResourceProvider;
import org.apache.deltaspike.core.util.BeanUtils;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract ExternalResourceProvider implementation with some basic utility functionality.
 */
public abstract class BaseResourceProvider implements ExternalResourceProvider
{
    private static final Logger logger = Logger.getLogger(BaseResourceProvider.class.getName());

    @Inject
    @Any
    private Instance<InjectionPoint> injectionPoint;

    protected boolean isXml()
    {
        final boolean isXml = BeanUtils.extractAnnotation(getInjectionPoint().getAnnotated(), XMLProperties.class)
                != null;
        return isXml;
    }

    protected InjectionPoint getInjectionPoint()
    {
        return this.injectionPoint.get();
    }

    protected Set<Annotation> getAnnotations()
    {
        return this.getInjectionPoint().getAnnotated().getAnnotations();
    }

    protected void loadInputStreamToProperties(InputStream inputStream, Properties properties, String name)
    {
        boolean isXml = this.isXml();
        try
        {
            if (isXml)
            {
                properties.loadFromXML(inputStream);
            }
            else
            {
                properties.load(inputStream);
            }
        }
        catch (IOException e)
        {
            logger.log(Level.WARNING,"Unable to read resource " + name,e);

        }
    }

    @Override
    public Properties readProperties(ExternalResource externalResource)
    {
        final Properties properties = new Properties();
        final String name = externalResource.location();
        final InputStream inputStream = this.readStream(externalResource);
        this.loadInputStreamToProperties(inputStream, properties, name);
        return properties;
    }
}
