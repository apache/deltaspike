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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.data.impl.meta.RepositoryComponent;
import org.apache.deltaspike.jpa.spi.entitymanager.ActiveEntityManagerHolder;

@ApplicationScoped
public class EntityManagerRefLookup
{
    @Inject
    private EntityManager entityManager;

    @Inject
    private ActiveEntityManagerHolder activeEntityManagerHolder;

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
                ref.setEntityManager(entityManager);
            }
        }

        if (repository.hasEntityManagerFlushMode())
        {
            ref.getEntityManager().setFlushMode(repository.getEntityManagerFlushMode());
        }

        return ref;
    }
}
