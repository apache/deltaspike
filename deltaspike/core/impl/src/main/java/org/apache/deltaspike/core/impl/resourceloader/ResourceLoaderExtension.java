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

import org.apache.deltaspike.core.api.resourceloader.ClasspathResourceProvider;
import org.apache.deltaspike.core.api.resourceloader.FileResourceProvider;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

/**
 * This is needed for certain class loading cases (EARs, external modules).
 * Simply registers additional resource loader classes to the context.
 */
//TODO re-visit it based on DELTASPIKE-472
public class ResourceLoaderExtension implements Extension
{
    public void addResourceLoaders(final BeforeBeanDiscovery beforeBeanDiscovery, final BeanManager beanManager)
    {
        beforeBeanDiscovery.addAnnotatedType(this.createAnnotatedType(ClasspathResourceProvider.class,beanManager));
        beforeBeanDiscovery.addAnnotatedType(this.createAnnotatedType(InjectableResourceProducer.class,beanManager));
        beforeBeanDiscovery.addAnnotatedType(this.createAnnotatedType(FileResourceProvider.class,beanManager));
    }

    private AnnotatedType<?> createAnnotatedType(final Class<?> clazz, final BeanManager beanManager)
    {
        return beanManager.createAnnotatedType(clazz);
    }
}
