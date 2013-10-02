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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.api.provider.DependentProvider;
import org.apache.deltaspike.data.api.EntityManagerResolver;
import org.apache.deltaspike.data.impl.meta.RepositoryComponent;

public class EntityManagerLookup
{

    @Inject
    @Any
    private Instance<EntityManager> entityManager;

    public EntityManager lookupFor(RepositoryComponent repository)
    {
        if (repository.hasEntityManagerResolver())
        {
            DependentProvider<? extends EntityManagerResolver> resolver =
                    lookupResolver(repository.getEntityManagerResolverClass());
            EntityManager result = resolver.get().resolveEntityManager();
            if (repository.getEntityManagerFlushMode() != null)
            {
                result.setFlushMode(repository.getEntityManagerFlushMode());
            }
            resolver.destroy();
        }
        return entityManager.get();
    }

    private DependentProvider<? extends EntityManagerResolver> lookupResolver(
            Class<? extends EntityManagerResolver> resolverClass)
    {
        DependentProvider<? extends EntityManagerResolver> resolver = BeanProvider.getDependent(resolverClass);
        return resolver;
    }
}
