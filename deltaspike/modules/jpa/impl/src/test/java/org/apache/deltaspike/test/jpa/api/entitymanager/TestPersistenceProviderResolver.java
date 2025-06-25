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
package org.apache.deltaspike.test.jpa.api.entitymanager;

import jakarta.enterprise.inject.Vetoed;
import jakarta.persistence.Cache;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.spi.PersistenceProvider;
import jakarta.persistence.spi.PersistenceProviderResolver;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.ProviderUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.deltaspike.test.jpa.api.shared.TestEntityManager;

/**
 * PersistenceProviderResolver for dummy PersistenceProviders.
 */
@Vetoed
public class TestPersistenceProviderResolver implements PersistenceProviderResolver
{
    private List<PersistenceProvider> persistenceProviders;

    public TestPersistenceProviderResolver()
    {
        this.persistenceProviders = new ArrayList<PersistenceProvider>();
        this.persistenceProviders.add(new DummyPersistenceProvider());
    }

    @Override
    public void clearCachedProviders()
    {
    }

    @Override
    public List<PersistenceProvider> getPersistenceProviders()
    {
        return persistenceProviders;
    }


    @Vetoed
    public static class DummyPersistenceProvider implements PersistenceProvider
    {
        @Override
        public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map map)
        {
            return new DummyEntityManagerFactory();
        }

        @Override
        public EntityManagerFactory createEntityManagerFactory(String emName, Map map)
        {
            return new DummyEntityManagerFactory(emName, map);
        }

        @Override
        public ProviderUtil getProviderUtil()
        {
            return null;  
        }

        @Override
        public void generateSchema(PersistenceUnitInfo arg0, Map arg1)
        {
            
        }

        @Override
        public boolean generateSchema(String arg0, Map arg1)
        {
            return true;
        }
    }

    @Vetoed
    public static class DummyEntityManagerFactory implements EntityManagerFactory
    {
        private final String emName;
        private final Map map;

        public DummyEntityManagerFactory()
        {
            this.emName = null;
            this.map = null;

        }

        public DummyEntityManagerFactory(String emName, Map map)
        {
            this.emName = emName;
            this.map = map;
        }


        @Override
        public void close()
        {
        }

        @Override
        public EntityManager createEntityManager()
        {
            return new TestEntityManager(emName);
        }

        @Override
        public EntityManager createEntityManager(Map map)
        {
            return new TestEntityManager(emName);
        }

        @Override
        public CriteriaBuilder getCriteriaBuilder()
        {
            return null;  
        }

        @Override
        public Metamodel getMetamodel()
        {
            return null;  
        }

        @Override
        public boolean isOpen()
        {
            return false;  
        }

        @Override
        public Map<String, Object> getProperties()
        {
            return map;
        }

        @Override
        public Cache getCache()
        {
            return null;  
        }

        @Override
        public PersistenceUnitUtil getPersistenceUnitUtil()
        {
            return null;  
        }

        @Override
        public EntityManager createEntityManager(SynchronizationType arg0)
        {
            return null;
        }

        @Override
        public EntityManager createEntityManager(SynchronizationType arg0, Map arg1)
        {
            return null;
        }

        @Override
        public void addNamedQuery(String arg0, Query arg1)
        {
            
        }

        @Override
        public <T> T unwrap(Class<T> arg0)
        {
            return null;
        }

        @Override
        public <T> void addNamedEntityGraph(String arg0, EntityGraph<T> arg1)
        {
            
        }
    }
}
