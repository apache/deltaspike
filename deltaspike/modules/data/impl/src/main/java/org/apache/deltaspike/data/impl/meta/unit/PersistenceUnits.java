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
package org.apache.deltaspike.data.impl.meta.unit;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.deltaspike.data.impl.meta.RepositoryEntity;

public final class PersistenceUnits
{

    private static PersistenceUnits instance = new PersistenceUnits();

    private List<PersistenceUnit> persistenceUnits = Collections.emptyList();

    private PersistenceUnits()
    {
    }

    public static PersistenceUnits instance()
    {
        return instance;
    }

    public void init()
    {
        persistenceUnits = readPersistenceXmls();
    }

    public boolean isEntity(Class<?> entityClass)
    {
        return find(entityClass) != null;
    }

    public String primaryKeyField(Class<?> entityClass)
    {
        EntityDescriptor entity = find(entityClass);
        if (entity != null)
        {
            return entity.getId();
        }
        return null;
    }

    public Class<?> primaryKeyIdClass(Class<?> entityClass)
    {
        EntityDescriptor entity = find(entityClass);
        if (entity != null && entity.getIdClass() != null)
        {
            return entity.getIdClass();
        }
        return null;
    }

    public String entityName(Class<?> entityClass)
    {
        EntityDescriptor entity = find(entityClass);
        if (entity != null)
        {
            return entity.getName();
        }
        return null;
    }

    public RepositoryEntity lookupMetadata(Class<?> entityClass)
    {
        EntityDescriptor entity = find(entityClass);
        if (entity != null)
        {
            return new RepositoryEntity(entityClass, entity.getIdClass());
        }
        return null;
    }

    private List<PersistenceUnit> readPersistenceXmls()
    {
        try
        {
            PersistenceUnitReader reader = new PersistenceUnitReader();
            return reader.readAll();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read persistence unit info", e);
        }
    }

    private EntityDescriptor find(Class<?> entityClass)
    {
        for (PersistenceUnit unit : persistenceUnits)
        {
            EntityDescriptor entity = unit.find(entityClass);
            if (entity != null)
            {
                return entity;
            }
        }
        return null;
    }

}
