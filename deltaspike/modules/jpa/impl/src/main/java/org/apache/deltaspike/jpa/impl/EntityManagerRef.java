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
package org.apache.deltaspike.jpa.impl;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Serializable entry which stores information about an {@link javax.persistence.EntityManager}
 */
public class EntityManagerRef implements Serializable
{
    private static final long serialVersionUID = -4273544531446327680L;

    private String key;

    private final Class sourceClass;
    private final String fieldName;

    private transient Field entityManagerField;

    EntityManagerRef(Class sourceClass, String fieldName, String key)
    {
        this.sourceClass = sourceClass;
        this.fieldName = fieldName;
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    public Class getSourceClass()
    {
        return sourceClass;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    //we can't cache the reference,
    //because this instance will be stored in a cache and we have to support dependent scoped entity managers
    public EntityManager getEntityManager(Object target)
    {
        if (this.entityManagerField == null)
        {
            try
            {
                this.entityManagerField = this.sourceClass.getDeclaredField(this.fieldName);
            }
            catch (NoSuchFieldException e)
            {
                //TODO add logging in case of project stage dev.
                return null;
            }
        }

        if (!entityManagerField.isAccessible())
        {
            entityManagerField.setAccessible(true);
        }

        try
        {
            @SuppressWarnings({ "UnnecessaryLocalVariable" })
            EntityManager entityManager = (EntityManager) entityManagerField.get(target);
            return entityManager;
        }
        catch (IllegalAccessException e)
        {
            //TODO add logging in case of project stage dev.
            return null;
        }
    }
}
