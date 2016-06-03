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
package org.apache.deltaspike.data.impl.handler;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.data.impl.meta.RepositoryComponent;
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
            this.globalEntityManagerInitialized = true;
            
            BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();
            Set<Bean<?>> beans = beanManager.getBeans(EntityManager.class);

            if (!beans.isEmpty() && beans.size() == 1)
            {
                Class<? extends Annotation> scope = beanManager.resolve(beans).getScope();
                globalEntityManagerIsNormalScope = beanManager.isNormalScope(scope);

                if (globalEntityManagerIsNormalScope)
                {
                    Bean<?> bean = beans.iterator().next();
                    globalEntityManager = (EntityManager) beanManager.getReference(bean,
                            EntityManager.class,
                            beanManager.createCreationalContext(bean));
                }
            }
            else
            {
                throw new IllegalArgumentException(
                        "None or multiple EntityManager's found with the default qualifier.");
            }            
        }
    }
    
    public EntityManagerRef lookupReference(final RepositoryComponent repository)
    {
        EntityManagerRef ref = new EntityManagerRef();

        if (repository.hasEntityManagerResolver())
        {
            ref.setEntityManagerResolverClass(
                    repository.getEntityManagerResolverClass());
            
            if (repository.isEntityManagerResolverIsNormalScope())
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

        if (repository.hasEntityManagerFlushMode())
        {
            ref.getEntityManager().setFlushMode(repository.getEntityManagerFlushMode());
        }

        return ref;
    }
}
