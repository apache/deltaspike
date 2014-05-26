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

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

import org.apache.deltaspike.data.api.EntityManagerDelegate;
import org.apache.deltaspike.data.spi.DelegateQueryHandler;
import org.apache.deltaspike.data.spi.QueryInvocationContext;

@SuppressWarnings("unchecked")
public class EntityManagerDelegateHandler<E> implements EntityManagerDelegate<E>, DelegateQueryHandler
{

    @Inject
    private QueryInvocationContext context;

    @Override
    public void persist(E entity)
    {
        entityManager().persist(entity);
    }

    @Override
    public E merge(E entity)
    {
        return entityManager().merge(entity);
    }

    @Override
    public E find(Object primaryKey, Map<String, Object> properties)
    {
        return (E) entityManager().find(context.getEntityClass(), primaryKey, properties);
    }

    @Override
    public E find(Object primaryKey, LockModeType lockMode)
    {
        return (E) entityManager().find(context.getEntityClass(), primaryKey, lockMode);
    }

    @Override
    public E find(Object primaryKey, LockModeType lockMode, Map<String, Object> properties)
    {
        return (E) entityManager().find(context.getEntityClass(), primaryKey, lockMode, properties);
    }

    @Override
    public E getReference(Object primaryKey)
    {
        return (E) entityManager().getReference(context.getEntityClass(), primaryKey);
    }

    @Override
    public void setFlushMode(FlushModeType flushMode)
    {
        entityManager().setFlushMode(flushMode);
    }

    @Override
    public FlushModeType getFlushMode()
    {
        return entityManager().getFlushMode();
    }

    @Override
    public void lock(Object entity, LockModeType lockMode)
    {
        entityManager().lock(entity, lockMode);
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties)
    {
        entityManager().lock(entity, lockMode, properties);
    }

    @Override
    public void refresh(E entity, Map<String, Object> properties)
    {
        entityManager().refresh(entity, properties);
    }

    @Override
    public void refresh(E entity, LockModeType lockMode)
    {
        entityManager().refresh(entity, lockMode);
    }

    @Override
    public void refresh(E entity, LockModeType lockMode, Map<String, Object> properties)
    {
        entityManager().refresh(entity, lockMode, properties);
    }

    @Override
    public void clear()
    {
        entityManager().clear();
    }

    @Override
    public void detach(E entity)
    {
        entityManager().detach(entity);
    }

    @Override
    public boolean contains(E entity)
    {
        return entityManager().contains(entity);
    }

    @Override
    public LockModeType getLockMode(E entity)
    {
        return entityManager().getLockMode(entity);
    }

    @Override
    public void setProperty(String propertyName, Object value)
    {
        entityManager().setProperty(propertyName, value);
    }

    @Override
    public Map<String, Object> getProperties()
    {
        return entityManager().getProperties();
    }

    @Override
    public void joinTransaction()
    {
        entityManager().joinTransaction();
    }

    @Override
    public <T> T unwrap(Class<T> cls)
    {
        return entityManager().unwrap(cls);
    }

    @Override
    public Object getDelegate()
    {
        return entityManager().getDelegate();
    }

    @Override
    public void close()
    {
        entityManager().close();
    }

    @Override
    public boolean isOpen()
    {
        return entityManager().isOpen();
    }

    @Override
    public EntityTransaction getTransaction()
    {
        return entityManager().getTransaction();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory()
    {
        return entityManager().getEntityManagerFactory();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder()
    {
        return entityManager().getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel()
    {
        return entityManager().getMetamodel();
    }

    private EntityManager entityManager()
    {
        return context.getEntityManager();
    }

}
