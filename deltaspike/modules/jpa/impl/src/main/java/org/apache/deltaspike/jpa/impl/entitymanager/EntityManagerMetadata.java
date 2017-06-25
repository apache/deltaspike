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

import org.apache.deltaspike.jpa.api.entitymanager.EntityManagerConfig;
import org.apache.deltaspike.jpa.api.entitymanager.EntityManagerResolver;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.persistence.FlushModeType;
import java.lang.annotation.Annotation;
import java.util.Set;

public class EntityManagerMetadata
{
    private Class<? extends EntityManagerResolver> entityManagerResolverClass;
    private Class<? extends Annotation>[] qualifiers;
    private boolean entityManagerResolverIsNormalScope;
    private FlushModeType entityManagerFlushMode;

    public Class<? extends EntityManagerResolver> getEntityManagerResolverClass()
    {
        return entityManagerResolverClass;
    }

    public void setEntityManagerResolverClass(Class<? extends EntityManagerResolver> entityManagerResolverClass)
    {
        this.entityManagerResolverClass = entityManagerResolverClass;
    }

    public FlushModeType getEntityManagerFlushMode()
    {
        return entityManagerFlushMode;
    }

    public void setEntityManagerFlushMode(FlushModeType entityManagerFlushMode)
    {
        this.entityManagerFlushMode = entityManagerFlushMode;
    }

    public boolean isEntityManagerResolverIsNormalScope()
    {
        return entityManagerResolverIsNormalScope;
    }

    public void setEntityManagerResolverIsNormalScope(boolean entityManagerResolverIsNormalScope)
    {
        this.entityManagerResolverIsNormalScope = entityManagerResolverIsNormalScope;
    }

    public Class<? extends Annotation>[] getQualifiers()
    {
        return qualifiers;
    }

    public void setQualifiers(Class<? extends Annotation>[] qualifiers)
    {
        this.qualifiers = qualifiers;
    }

    public boolean readFrom(Class<?> componentClass, BeanManager beanManager)
    {
        EntityManagerConfig entityManagerConfig = componentClass.getAnnotation(EntityManagerConfig.class);
        boolean processed = false;
        if (entityManagerConfig != null)
        {
            processed = true;
            this.setEntityManagerFlushMode(entityManagerConfig.flushMode());
            this.setQualifiers(entityManagerConfig.qualifier());
            Class<? extends EntityManagerResolver> resolverClass = entityManagerConfig.entityManagerResolver();
            if (!resolverClass.equals(EntityManagerResolver.class))
            {
                this.setEntityManagerResolverClass(resolverClass);
                Set<Bean<?>> beans = beanManager.getBeans(resolverClass);
                Class<? extends Annotation> scope = beanManager.resolve(beans).getScope();
                this.setEntityManagerResolverIsNormalScope(beanManager.isNormalScope(scope));
            }
            else
            {
                this.setEntityManagerResolverIsNormalScope(false);
            }
        }

        Transactional transactional = componentClass.getAnnotation(Transactional.class);

        if (transactional != null && this.qualifiers == null)
        {
            processed = true;
            this.setQualifiers(transactional.qualifier());
        }

        return processed;
    }

}

