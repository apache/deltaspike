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
package org.apache.deltaspike.data.impl.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.persistence.FlushModeType;

import org.apache.deltaspike.data.api.EntityManagerConfig;
import org.apache.deltaspike.data.api.EntityManagerResolver;

@ApplicationScoped
public class RepositoryMetadataInitializer
{
    private static final Logger log = Logger.getLogger(RepositoryMetadataInitializer.class.getName());

    @Inject
    private RepositoryMethodMetadataInitializer methodMetadataInitializer;
    
    @Inject
    private EntityMetadataInitializer entityMetadataInitializer;
    
    public RepositoryMetadata init(Class<?> repositoryClass, BeanManager beanManager)
    {
        RepositoryMetadata repositoryMetadata = new RepositoryMetadata(repositoryClass);
        
        repositoryMetadata.setEntityManagerResolverClass(extractEntityManagerResolver(repositoryClass));
        repositoryMetadata.setEntityManagerFlushMode(extractEntityManagerFlushMode(repositoryClass));

        if (repositoryMetadata.getEntityManagerResolverClass() != null)
        {
            Set<Bean<?>> beans = beanManager.getBeans(repositoryMetadata.getEntityManagerResolverClass());
            Class<? extends Annotation> scope = beanManager.resolve(beans).getScope();

            repositoryMetadata.setEntityManagerResolverIsNormalScope(beanManager.isNormalScope(scope));
        }
        else
        {
            repositoryMetadata.setEntityManagerResolverIsNormalScope(false);
        }
        
        repositoryMetadata.setEntityMetadata(entityMetadataInitializer.init(repositoryMetadata));

        initializeMethodsMetadata(repositoryMetadata, beanManager);
        
        return repositoryMetadata;
    }

    private void initializeMethodsMetadata(RepositoryMetadata repositoryMetadata, BeanManager beanManager)
    {
        repositoryMetadata.setMethodsMetadata(new HashMap<Method, RepositoryMethodMetadata>());
        
        Set<Class<?>> allImplemented = new HashSet<Class<?>>();
        collectClasses(repositoryMetadata.getRepositoryClass(), allImplemented);
        log.log(Level.FINER, "collectClasses(): Found {0} for {1}",
                new Object[] { allImplemented, repositoryMetadata.getRepositoryClass() });

        for (Class<?> implemented : allImplemented)
        {
            Method[] repositoryMethods = implemented.getDeclaredMethods();
            for (Method repositoryMethod : repositoryMethods)
            {
                RepositoryMethodMetadata methodMetadata =
                        methodMetadataInitializer.init(repositoryMetadata, repositoryMethod, beanManager);
                repositoryMetadata.getMethodsMetadata().put(repositoryMethod, methodMetadata);
            }
        }
    }

    private void collectClasses(Class<?> cls, Set<Class<?>> result)
    {
        if (cls == null || cls == Object.class)
        {
            return;
        }
        
        result.add(cls);
        
        for (Class<?> child : cls.getInterfaces())
        {
            collectClasses(child, result);
        }
        
        collectClasses(cls.getSuperclass(), result);
    }

    private Class<? extends EntityManagerResolver> extractEntityManagerResolver(Class<?> clazz)
    {
        EntityManagerConfig config = extractEntityManagerConfig(clazz);
        if (config != null && !EntityManagerResolver.class.equals(config.entityManagerResolver()))
        {
            return config.entityManagerResolver();
        }
        return null;
    }

    private FlushModeType extractEntityManagerFlushMode(Class<?> clazz)
    {
        EntityManagerConfig config = extractEntityManagerConfig(clazz);
        if (config != null)
        {
            return config.flushMode();
        }
        return null;
    }

    private EntityManagerConfig extractEntityManagerConfig(Class<?> clazz)
    {
        if (clazz.isAnnotationPresent(EntityManagerConfig.class))
        {
            return clazz.getAnnotation(EntityManagerConfig.class);
        }
        return null;
    }


}
