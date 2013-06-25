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

import java.io.Serializable;

import org.apache.deltaspike.data.impl.property.query.NamedPropertyCriteria;
import org.apache.deltaspike.data.impl.property.query.PropertyQueries;
import org.apache.deltaspike.data.impl.property.query.PropertyQuery;

abstract class PersistentClassDescriptor
{

    protected final String name;
    protected final Class<?> entityClass;
    protected final Class<? extends Serializable> idClass;
    protected final String id;
    private PersistentClassDescriptor parent;

    PersistentClassDescriptor(String name, String packageName, String className, String idClass, String id)
    {
        Class<?> clazz = entityClass(className, packageName);
        this.name = name;
        this.entityClass = clazz;
        this.idClass = idClass(clazz, idClass, packageName, id);
        this.id = id;
    }

    public Class<? extends Serializable> getIdClass()
    {
        return idClass;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public Class<?> getEntityClass()
    {
        return entityClass;
    }

    String className(Class<?> clazz)
    {
        return clazz == null ? null : clazz.getSimpleName();
    }

    private Class<?> entityClass(String entityClass, String packageName)
    {
        try
        {
            String clazzName = buildClassName(entityClass, packageName);
            return Class.forName(clazzName);
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalArgumentException("Can't create class " + buildClassName(entityClass, packageName), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Serializable> idClass(Class<?> entity, String idClass, String packageName, String id)
    {
        try
        {
            return (Class<? extends Serializable>) (idClass != null ? Class
                    .forName(buildClassName(idClass, packageName)) : lookupIdClass(entity, id));
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalArgumentException("Failed to get ID class", e);
        }
    }

    private Class<?> lookupIdClass(Class<?> entity, String id)
    {
        if (entity == null || id == null)
        {
            return null;
        }
        PropertyQuery<Serializable> query = PropertyQueries.<Serializable> createQuery(entity)
                .addCriteria(new NamedPropertyCriteria(id));
        return query.getFirstResult().getJavaClass();
    }

    private String buildClassName(String clazzName, String packageName)
    {
        if (clazzName == null && packageName == null)
        {
            return null;
        }
        return (packageName != null && !isClassNameQualified(clazzName)) ? packageName + "." + clazzName : clazzName;
    }

    private boolean isClassNameQualified(String name)
    {
        return name.contains(".");
    }

    public PersistentClassDescriptor getParent()
    {
        return parent;
    }

    public void setParent(PersistentClassDescriptor parent)
    {
        this.parent = parent;
    }

}
