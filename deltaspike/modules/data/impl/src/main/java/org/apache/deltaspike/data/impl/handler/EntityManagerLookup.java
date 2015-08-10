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

import org.apache.deltaspike.core.api.literal.DefaultLiteral;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.api.provider.DependentProvider;
import org.apache.deltaspike.data.api.EntityManagerResolver;
import org.apache.deltaspike.data.impl.meta.RepositoryComponent;

public class EntityManagerLookup
{

    @Inject
    @Any
    private Instance<EntityManager> entityManager;

    private DependentProvider<? extends EntityManagerResolver> dependentResolverProvider;

    public EntityManager lookupFor(final RepositoryComponent repository)
    {
        EntityManager result = null;
        if (repository.hasEntityManagerResolver())
        {
            final Class<? extends EntityManagerResolver> emrc = repository.getEntityManagerResolverClass();
            if (!repository.isEntityManagerResolverIsNormalScope())
            {
                final DependentProvider<? extends EntityManagerResolver> resolver = lookupResolver(emrc);
                dependentResolverProvider = resolver;
                result = resolver.get().resolveEntityManager();
            }
            else
            {
                result = BeanProvider.getContextualReference(emrc).resolveEntityManager();
            }
        }
        else
        {
            result = entityManager.select(new DefaultLiteral()).get();
        }
        if (repository.hasEntityManagerFlushMode())
        {
            result.setFlushMode(repository.getEntityManagerFlushMode());
        }
        return result;
    }

    public void release()
    {
        if (dependentResolverProvider != null)
        {
            dependentResolverProvider.destroy();
        }
    }

    private DependentProvider<? extends EntityManagerResolver> lookupResolver(
            Class<? extends EntityManagerResolver> resolverClass)
    {
        return BeanProvider.getDependent(resolverClass);
    }
}
