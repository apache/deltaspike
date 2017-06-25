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

package org.apache.deltaspike.jpa.impl.entitymanager;

import javax.persistence.EntityManager;
import org.apache.deltaspike.core.api.provider.DependentProvider;
import org.apache.deltaspike.jpa.api.entitymanager.EntityManagerResolver;

public class EntityManagerRef
{
    private EntityManager entityManager;
    private DependentProvider<? extends EntityManager> entityManagerDependentProvider;
    
    private Class<? extends EntityManagerResolver> entityManagerResolverClass;
    private EntityManagerResolver entityManagerResolver;
    private DependentProvider<? extends EntityManagerResolver> entityManagerResolverDependentProvider;
        
    public void release()
    {
        if (entityManagerDependentProvider != null)
        {
            entityManagerDependentProvider.destroy();
        }
        
        if (entityManagerResolverDependentProvider != null)
        {
            entityManagerResolverDependentProvider.destroy();
        }
    }

    public Class<? extends EntityManagerResolver> getEntityManagerResolverClass()
    {
        return entityManagerResolverClass;
    }

    public void setEntityManagerResolverClass(Class<? extends EntityManagerResolver> entityManagerResolverClass)
    {
        this.entityManagerResolverClass = entityManagerResolverClass;
    }

    public DependentProvider<? extends EntityManagerResolver> getEntityManagerResolverDependentProvider()
    {
        return entityManagerResolverDependentProvider;
    }

    public void setEntityManagerResolverDependentProvider(
            DependentProvider<? extends EntityManagerResolver> entityManagerResolverDependentProvider)
    {
        this.entityManagerResolverDependentProvider = entityManagerResolverDependentProvider;
    }

    public EntityManager getEntityManager()
    {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    public EntityManagerResolver getEntityManagerResolver()
    {
        return entityManagerResolver;
    }

    public void setEntityManagerResolver(EntityManagerResolver entityManagerResolver)
    {
        this.entityManagerResolver = entityManagerResolver;
    }

    public DependentProvider<? extends EntityManager> getEntityManagerDependentProvider()
    {
        return entityManagerDependentProvider;
    }

    public void setEntityManagerDependentProvider(
            DependentProvider<? extends EntityManager> entityManagerDependentProvider)
    {
        this.entityManagerDependentProvider = entityManagerDependentProvider;
    }
}
