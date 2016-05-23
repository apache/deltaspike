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
package org.apache.deltaspike.data.impl.util;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.persistence.metamodel.EntityType;
import org.apache.deltaspike.core.util.StringUtils;

import org.apache.deltaspike.data.impl.property.Property;
import org.apache.deltaspike.data.impl.property.query.AnnotatedPropertyCriteria;
import org.apache.deltaspike.data.impl.property.query.NamedPropertyCriteria;
import org.apache.deltaspike.data.impl.property.query.PropertyCriteria;
import org.apache.deltaspike.data.impl.property.query.PropertyQueries;
import org.apache.deltaspike.data.impl.property.query.PropertyQuery;
import org.apache.deltaspike.jpa.spi.descriptor.xml.PersistenceUnitDescriptorProvider;

public final class EntityUtils
{

    private EntityUtils()
    {
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Class<? extends Serializable> primaryKeyClass(Class<?> entityClass)
    {
        if (entityClass.isAnnotationPresent(IdClass.class))
        {
            return entityClass.getAnnotation(IdClass.class).value(); // Serializablity isn't required, could cause
                                                                     // problems
        }
        Class clazz = PersistenceUnitDescriptorProvider.getInstance().primaryKeyIdClass(entityClass);
        if (clazz != null)
        {
            return clazz;
        }
        Property<Serializable> property = primaryKeyProperty(entityClass);
        return property.getJavaClass();
    }

    public static Object primaryKeyValue(Object entity)
    {
        Property<Serializable> property = primaryKeyProperty(entity.getClass());
        return primaryKeyValue(entity, property);
    }

    public static Object primaryKeyValue(Object entity, Property<Serializable> primaryKeyProperty)
    {
        return primaryKeyProperty.getValue(entity);
    }
    
    public static String entityName(Class<?> entityClass)
    {
        String result = null;
        if (entityClass.isAnnotationPresent(Entity.class))
        {
            result = entityClass.getAnnotation(Entity.class).name();
        }
        else
        {
            result = PersistenceUnitDescriptorProvider.getInstance().entityName(entityClass);
        }
        return (result != null && !"".equals(result)) ? result : entityClass.getSimpleName();
    }

    public static String tableName(Class<?> entityClass, EntityManager entityManager)
    {
        String tableName = PersistenceUnitDescriptorProvider.getInstance().entityTableName(entityClass);
        if (StringUtils.isEmpty(tableName))
        {
            Table tableAnnotation = entityClass.getAnnotation(Table.class);
            if (tableAnnotation != null && StringUtils.isNotEmpty(tableAnnotation.name()))
            {
                return tableAnnotation.name();
            }

            EntityType<?> entityType = entityManager.getMetamodel().entity(entityClass);
            return entityType.getName();
        }
        return tableName;
    }

    public static boolean isEntityClass(Class<?> entityClass)
    {
        return entityClass.isAnnotationPresent(Entity.class)
                || PersistenceUnitDescriptorProvider.getInstance().isEntity(entityClass);
    }

    public static Property<Serializable> primaryKeyProperty(Class<?> entityClass)
    {
        for (PropertyCriteria c : primaryKeyPropertyCriteriaList(entityClass))
        {
            PropertyQuery<Serializable> query = PropertyQueries.<Serializable> createQuery(entityClass)
                    .addCriteria(c);
            if (query.getFirstResult() != null)
            {
                return query.getFirstResult();
            }
        }
        throw new IllegalStateException("Class " + entityClass + " has no id defined");
    }

    private static List<PropertyCriteria> primaryKeyPropertyCriteriaList(Class<?> entityClass)
    {
        List<PropertyCriteria> criteria = new LinkedList<PropertyCriteria>();
        criteria.add(new AnnotatedPropertyCriteria(Id.class));
        criteria.add(new AnnotatedPropertyCriteria(EmbeddedId.class));
        String[] fromMappingFiles = PersistenceUnitDescriptorProvider.getInstance().primaryKeyFields(entityClass);
        if (fromMappingFiles != null)
        {
            for (String id : fromMappingFiles)
            {
                criteria.add(new NamedPropertyCriteria(id));
            }
        }
        return criteria;
    }

    public static Property<Serializable> getVersionProperty(Class<?> entityClass)
    {
        List<PropertyCriteria> criteriaList = new LinkedList<PropertyCriteria>();
        criteriaList.add(new AnnotatedPropertyCriteria(Version.class));

        String fromMappingFiles = PersistenceUnitDescriptorProvider.getInstance().versionField(entityClass);
        if (fromMappingFiles != null)
        {
            criteriaList.add(new NamedPropertyCriteria(fromMappingFiles));
        }

        for (PropertyCriteria criteria : criteriaList)
        {
            PropertyQuery<Serializable> query =
                PropertyQueries.<Serializable> createQuery(entityClass).addCriteria(criteria);
            Property<Serializable> result = query.getFirstResult();
            if (result != null)
            {
                return result;
            }
        }

        return null;
    }
}
