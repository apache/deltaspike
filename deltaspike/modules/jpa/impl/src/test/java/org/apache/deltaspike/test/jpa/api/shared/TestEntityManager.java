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
package org.apache.deltaspike.test.jpa.api.shared;

import javax.enterprise.inject.Typed;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;
import java.util.Map;

@Typed()
public class TestEntityManager implements EntityManager
{
    private EntityTransaction entityTransaction = new TestEntityTransaction(this);

    private boolean open = true;
    private boolean flushed = false;
    private String unitName = null;

    public TestEntityManager()
    {
    }

    public TestEntityManager(String name)
    {
        this.unitName = name;
    }

    public String getUnitName()
    {
        return unitName;
    }

    @Override
    public void persist(Object entity)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public <T> T merge(T entity)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void remove(Object entity)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void flush()
    {
        flushed = true;
    }

    @Override
    public void setFlushMode(FlushModeType flushMode)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public FlushModeType getFlushMode()
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void lock(Object entity, LockModeType lockMode)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void refresh(Object entity)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void clear()
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void detach(Object entity)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public boolean contains(Object entity)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public LockModeType getLockMode(Object entity)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void setProperty(String propertyName, Object value)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Map<String, Object> getProperties()
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Query createQuery(String qlString)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Query createNamedQuery(String name)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Query createNativeQuery(String sqlString)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultClass)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void joinTransaction()
    {
        // all fine, nothing to do
    }

    @Override
    public <T> T unwrap(Class<T> cls)
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Object getDelegate()
    {
        return this;
    }

    @Override
    public void close()
    {
        if (!open)
        {
            throw new IllegalStateException("entity manager is closed already");
        }
        open = false;
    }

    @Override
    public boolean isOpen()
    {
        return open;
    }

    @Override
    public EntityTransaction getTransaction()
    {
        return entityTransaction;
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory()
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder()
    {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Metamodel getMetamodel()
    {
        throw new IllegalStateException("not implemented");
    }

    public boolean isFlushed()
    {
        return flushed;
    }

    public void setFlushed(boolean flushed) {
        this.flushed = flushed;
    }
}
