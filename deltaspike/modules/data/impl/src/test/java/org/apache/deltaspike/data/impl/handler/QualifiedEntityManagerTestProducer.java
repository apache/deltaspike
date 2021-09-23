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

import java.util.Map;

import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.metamodel.Metamodel;

import org.apache.deltaspike.data.test.service.Simplistic;

public class QualifiedEntityManagerTestProducer
{

    @Produces
    @Simplistic
    private final EntityManager entityManager = new EntityManager()
    {

        @Override
        public <T> T unwrap(Class<T> arg0)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setProperty(String arg0, Object arg1)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setFlushMode(FlushModeType arg0)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(Object arg0)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void refresh(Object arg0, LockModeType arg1, Map<String, Object> arg2)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void refresh(Object arg0, LockModeType arg1)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void refresh(Object arg0, Map<String, Object> arg1)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void refresh(Object arg0)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void persist(Object arg0)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T merge(T arg0)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void lock(Object arg0, LockModeType arg1, Map<String, Object> arg2)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void lock(Object arg0, LockModeType arg1)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void joinTransaction()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isOpen()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public EntityTransaction getTransaction()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T getReference(Class<T> arg0, Object arg1)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Object> getProperties()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Metamodel getMetamodel()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public LockModeType getLockMode(Object arg0)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public FlushModeType getFlushMode()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public EntityManagerFactory getEntityManagerFactory()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getDelegate()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public CriteriaBuilder getCriteriaBuilder()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flush()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2, Map<String, Object> arg3)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T find(Class<T> arg0, Object arg1, Map<String, Object> arg2)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T find(Class<T> arg0, Object arg1)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void detach(Object arg0)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> TypedQuery<T> createQuery(String arg0, Class<T> arg1)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> TypedQuery<T> createQuery(CriteriaQuery<T> arg0)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Query createQuery(String arg0)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Query createNativeQuery(String arg0, String arg1)
        {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Query createNativeQuery(String arg0, Class arg1)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Query createNativeQuery(String arg0)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> TypedQuery<T> createNamedQuery(String arg0, Class<T> arg1)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Query createNamedQuery(String arg0)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object arg0)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear()
        {
            throw new UnsupportedOperationException();
        }
    };

}
