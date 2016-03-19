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

import java.io.Serializable;

public abstract class AbstractEntityDescriptor
{
    private String id[];
    private String version;
    private String name;
    private Class<?> entityClass;
    private Class<? extends Serializable> idClass;

    private AbstractEntityDescriptor parent;

    public AbstractEntityDescriptor()
    {
        
    }
    
    public AbstractEntityDescriptor(String[] id, String version, String name, Class<?> entityClass,
            Class<? extends Serializable> idClass, AbstractEntityDescriptor parent)
    {
        this.id = id;
        this.version = version;
        this.name = name;
        this.entityClass = entityClass;
        this.idClass = idClass;
        this.parent = parent;
    }

    public String[] getId()
    {
        return id;
    }

    public void setId(String[] id)
    {
        this.id = id;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Class<?> getEntityClass()
    {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass)
    {
        this.entityClass = entityClass;
    }

    public Class<? extends Serializable> getIdClass()
    {
        return idClass;
    }

    public void setIdClass(Class<? extends Serializable> idClass)
    {
        this.idClass = idClass;
    }

    public AbstractEntityDescriptor getParent()
    {
        return parent;
    }

    public void setParent(AbstractEntityDescriptor parent)
    {
        this.parent = parent;
    }
}
