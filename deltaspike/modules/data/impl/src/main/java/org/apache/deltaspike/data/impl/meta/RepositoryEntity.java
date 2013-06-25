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

/**
 * Data structure to store information about an entity:
 * <ul>
 * <li>Stores the class of the entity</li>
 * <li>Stores the primary key class</li>
 * </ul>
 *
 * @author thomashug
 */
public class RepositoryEntity
{

    private Class<?> entityClass;
    private Class<? extends Serializable> primaryClass;

    public RepositoryEntity(Class<?> entityClass)
    {
        this(entityClass, null);
    }

    public RepositoryEntity(Class<?> entityClass, Class<? extends Serializable> primaryClass)
    {
        this.entityClass = entityClass;
        this.primaryClass = primaryClass;
    }

    public Class<?> getEntityClass()
    {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass)
    {
        this.entityClass = entityClass;
    }

    public Class<? extends Serializable> getPrimaryClass()
    {
        return primaryClass;
    }

    public void setPrimaryClass(Class<? extends Serializable> primaryClass)
    {
        this.primaryClass = primaryClass;
    }

}
