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
package org.apache.deltaspike.jpa.spi.descriptor.xml;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.enterprise.inject.Typed;
import org.apache.deltaspike.core.util.StringUtils;

@Typed
public final class PersistenceUnitDescriptorProvider
{
    private static final PersistenceUnitDescriptorProvider INSTANCE = new PersistenceUnitDescriptorProvider();

    private final PersistenceUnitDescriptorParser persistenceUnitDescriptorParser
        = new PersistenceUnitDescriptorParser();
    
    private List<PersistenceUnitDescriptor> persistenceUnitDescriptors = Collections.emptyList();

    private PersistenceUnitDescriptorProvider()
    {
    }

    public static PersistenceUnitDescriptorProvider getInstance()
    {
        return INSTANCE;
    }

    public void init()
    {
        try
        {
            persistenceUnitDescriptors = persistenceUnitDescriptorParser.readAll();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to parse persitence.xml's", e);
        }
    }

    public PersistenceUnitDescriptor get(String name)
    {        
        for (PersistenceUnitDescriptor unit : persistenceUnitDescriptors)
        {
            if (name.equalsIgnoreCase(unit.getName()))
            {
                return unit;
            }
        }
        return null;
    }

    public boolean isEntity(Class<?> entityClass)
    {        
        return find(entityClass) != null;
    }

    public String[] primaryKeyFields(Class<?> entityClass)
    {        
        EntityDescriptor entity = find(entityClass);
        if (entity != null)
        {
            if (entity.getId() != null)
            {
                return entity.getId();
            }
            
            AbstractEntityDescriptor parent = entity.getParent();
            while (parent != null)
            {
                if (parent.getId() != null)
                {
                    return parent.getId();
                }
                
                parent = parent.getParent();
            }
        }
        return null;
    }

    public String versionField(Class<?> entityClass)
    {        
        EntityDescriptor entity = find(entityClass);
        if (entity != null)
        {            
            if (!StringUtils.isEmpty(entity.getVersion()))
            {
                return entity.getVersion();
            }
            
            AbstractEntityDescriptor parent = entity.getParent();
            while (parent != null)
            {
                if (!StringUtils.isEmpty(parent.getVersion()))
                {
                    return parent.getVersion();
                }
                
                parent = parent.getParent();
            }
        }
        return null;
    }

    public Class<?> primaryKeyIdClass(Class<?> entityClass)
    {        
        EntityDescriptor entity = find(entityClass);
        if (entity != null)
        {
            if (entity.getIdClass() != null)
            {
                return entity.getIdClass();
            }
            
            AbstractEntityDescriptor parent = entity.getParent();
            while (parent != null)
            {
                if (parent.getIdClass() != null)
                {
                    return parent.getIdClass();
                }
                
                parent = parent.getParent();
            }
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
    
    public String entityTableName(Class<?> entityClass)
    {        
        EntityDescriptor entity = find(entityClass);
        if (entity != null)
        {
            return entity.getTableName();
        }
        return null;
    }

    public EntityDescriptor find(Class<?> entityClass)
    {
        for (PersistenceUnitDescriptor unit : persistenceUnitDescriptors)
        {
            EntityDescriptor entity = find(entityClass, unit);
            if (entity != null)
            {
                return entity;
            }
        }
        return null;
    }
    
    protected EntityDescriptor find(Class<?> entityClass, PersistenceUnitDescriptor descriptor)
    {
        for (EntityDescriptor entity : descriptor.getEntityDescriptors())
        {
            if (entity.getEntityClass().equals(entityClass))
            {
                return entity;
            }
        }
        return null;
    }
}
