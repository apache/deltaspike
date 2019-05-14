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

import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.jpa.api.entitymanager.EntityManagerResolver;
import org.apache.deltaspike.jpa.spi.entitymanager.ActiveEntityManagerHolder;

@ApplicationScoped
public class EntityManagerRefLookup
{
    @Inject
    private ActiveEntityManagerHolder activeEntityManagerHolder;

    private volatile Boolean globalEntityManagerInitialized;
    private boolean globalEntityManagerIsNormalScope;
    private EntityManager globalEntityManager;
    
    private void lazyInitGlobalEntityManager()
    {
        if (this.globalEntityManagerInitialized == null)
        {
            initGlobalEntityManager();
        }
    }

    private synchronized void initGlobalEntityManager()
    {
        // switch into paranoia mode
        if (this.globalEntityManagerInitialized == null)
        {
            BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();
            Set<Bean<?>> beans = beanManager.getBeans(EntityManager.class);
            Bean<?> bean = beanManager.resolve(beans);

            if (bean == null)
            {
                throw new IllegalStateException("Could not find EntityManager with default qualifier.");
            }
            
            globalEntityManagerIsNormalScope = beanManager.isNormalScope(bean.getScope());
            if (globalEntityManagerIsNormalScope)
            {
                globalEntityManager = (EntityManager) beanManager.getReference(bean,
                        EntityManager.class,
                        beanManager.createCreationalContext(bean));       
            }

            this.globalEntityManagerInitialized = true;
        }
    }
    
    public EntityManagerRef lookupReference(final EntityManagerMetadata entityManagerMetadata)
    {
        EntityManagerRef ref = new EntityManagerRef();
        EntityManagerResolver unmanagedResolver = entityManagerMetadata.getUnmanagedResolver();
        if (unmanagedResolver != null)
        {
            ref.setEntityManagerResolver(unmanagedResolver);
            ref.setEntityManager(unmanagedResolver.resolveEntityManager());
        }
        else if (entityManagerMetadata.getEntityManagerResolverClass() != null)
        {
            ref.setEntityManagerResolverClass(entityManagerMetadata.getEntityManagerResolverClass());

            if (entityManagerMetadata.isEntityManagerResolverIsNormalScope())
            {
                ref.setEntityManagerResolver(
                        BeanProvider.getContextualReference(ref.getEntityManagerResolverClass()));
            }
            else
            {
                ref.setEntityManagerResolverDependentProvider(
                        BeanProvider.getDependent(ref.getEntityManagerResolverClass()));

                ref.setEntityManagerResolver(
                        ref.getEntityManagerResolverDependentProvider().get());
            }
            
            ref.setEntityManager(
                    ref.getEntityManagerResolver().resolveEntityManager());
        }
        else
        {
            if (activeEntityManagerHolder.isSet())
            {
                ref.setEntityManager(
                        activeEntityManagerHolder.get());
                
                // TODO should we really not apply the FlushMode on the active EntityManager?
                return ref;
            }
            else
            {
                lazyInitGlobalEntityManager();
                if (globalEntityManagerIsNormalScope)
                {
                    ref.setEntityManager(globalEntityManager);
                }
                else
                {
                    ref.setEntityManagerDependentProvider(
                            BeanProvider.getDependent(EntityManager.class));
                    ref.setEntityManager(
                            ref.getEntityManagerDependentProvider().get());
                }
            }
        }

        if (entityManagerMetadata.getEntityManagerFlushMode() != null)
        {
            ref.getEntityManager().setFlushMode(entityManagerMetadata.getEntityManagerFlushMode());
        }

        return ref;
    }
}
