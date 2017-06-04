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
package org.apache.deltaspike.data.impl.meta;

import java.io.Serializable;
import org.apache.deltaspike.data.impl.property.Property;

public class EntityMetadata
{
    private Class<?> entityClass;
    private Property<Serializable> primaryKeyProperty;
    private Class<? extends Serializable> primaryKeyClass;
    private Property<Serializable> versionProperty;
    private String entityName;

    public EntityMetadata(Class<?> entityClass)
    {
        this.entityClass = entityClass;
    }
    
    public EntityMetadata(Class<?> entityClass,
            Class<? extends Serializable> primaryKeyClass)
    {
        this.entityClass = entityClass;
        this.primaryKeyClass = primaryKeyClass;
    }
    
    public EntityMetadata(Class<?> entityClass, String entityName,
            Class<? extends Serializable> primaryKeyClass)
    {
        this.entityClass = entityClass;
        this.entityName = entityName;
        this.primaryKeyClass = primaryKeyClass;
    }
    
    public Class<?> getEntityClass()
    {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass)
    {
        this.entityClass = entityClass;
    }

    public Class<? extends Serializable> getPrimaryKeyClass()
    {
        return primaryKeyClass;
    }

    public void setPrimaryKeyClass(Class<? extends Serializable> primaryKeyClass)
    {
        this.primaryKeyClass = primaryKeyClass;
    }

    public Property<Serializable> getPrimaryKeyProperty()
    {
        return primaryKeyProperty;
    }

    public void setPrimaryKeyProperty(Property<Serializable> primaryKeyProperty)
    {
        this.primaryKeyProperty = primaryKeyProperty;
    }

    public Property<Serializable> getVersionProperty()
    {
        return versionProperty;
    }

    public void setVersionProperty(Property<Serializable> versionProperty)
    {
        this.versionProperty = versionProperty;
    }

    public String getEntityName()
    {
        return entityName;
    }

    public void setEntityName(String entityName)
    {
        this.entityName = entityName;
    }
}
