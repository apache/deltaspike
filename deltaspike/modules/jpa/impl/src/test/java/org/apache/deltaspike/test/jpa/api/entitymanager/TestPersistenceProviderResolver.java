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

import javax.enterprise.inject.Typed;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.deltaspike.test.jpa.api.shared.TestEntityManager;

/**
 * PersistenceProviderResolver for dummy PersistenceProviders.
 */
@Typed()
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


    @Typed()
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
    }

    @Typed()
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
    }
}
