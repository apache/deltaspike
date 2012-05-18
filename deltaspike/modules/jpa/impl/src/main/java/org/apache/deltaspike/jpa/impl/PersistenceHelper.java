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

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.util.ClassUtils;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanManager;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper which provides util methods for
 * {@link org.apache.deltaspike.jpa.impl.transaction.TransactionalInterceptorStrategy}
 */
@Typed()
public class PersistenceHelper
{
    private static final String NO_FIELD_MARKER = PersistenceHelper.class.getName() + ":DEFAULT_FIELD";

    private static transient volatile Map<ClassLoader, Map<String, EntityManagerRef>> persistenceContextMetaEntries =
        new ConcurrentHashMap<ClassLoader, Map<String, EntityManagerRef>>();

    private PersistenceHelper()
    {
        //prevent instantiation
    }

    /**
     * Analyzes the given instance and returns the found reference to an injected {@link EntityManager}
     * or null otherwise
     *
     * @param target instance to analyze
     * @return the injected entity-manager or null otherwise
     */
    public static List<EntityManagerRef> tryToFindEntityManagerReference(Object target)
    {
        List<EntityManagerRef> entityManagers = tryToFindEntityManagerEntryInTarget(target);

        if (entityManagers == null || entityManagers.isEmpty())
        {
            return null;
        }
        return entityManagers;
    }

    /*
     * needed for special add-ons - don't change it!
     */
    static List<EntityManagerRef> tryToFindEntityManagerEntryInTarget(Object target)
    {
        Map<String, EntityManagerRef> mapping = persistenceContextMetaEntries.get(getClassLoader());

        mapping = initMapping(mapping);

        String key = target.getClass().getName();
        EntityManagerRef entityManagerRef = mapping.get(key);

        if (entityManagerRef != null && entityManagerRef instanceof PersistenceContextMetaEntry &&
                NO_FIELD_MARKER.equals(entityManagerRef.getFieldName()))
        {
            return null;
        }

        if (entityManagerRef == null)
        {
            List<EntityManagerRef> foundEntries = findEntityManagerMetaReferences(target.getClass());

            if (foundEntries.isEmpty())
            {
                mapping.put(key, new PersistenceContextMetaEntry(
                        Object.class, NO_FIELD_MARKER, Default.class.getName(), false));
                return null;
            }

            if (foundEntries.size() == 1)
            {
                entityManagerRef = foundEntries.iterator().next();
            }
            else
            {
                //TODO remove workaround
                entityManagerRef = new EntityManagerRefHolder(foundEntries);
            }
            mapping.put(key, entityManagerRef);
        }

        List<EntityManagerRef> result = new ArrayList<EntityManagerRef>();

        if (entityManagerRef instanceof EntityManagerRefHolder)
        {
            for (EntityManagerRef currentRef : ((EntityManagerRefHolder) entityManagerRef).getEntityManagerRefs())
            {
                result.add(currentRef);
            }
        }
        else
        {
            result.add(entityManagerRef);
        }
        return result;
    }

    private static synchronized Map<String, EntityManagerRef> initMapping(
            Map<String, EntityManagerRef> mapping)
    {
        if (mapping == null)
        {
            mapping = new ConcurrentHashMap<String, EntityManagerRef>();
            persistenceContextMetaEntries.put(getClassLoader(), mapping);
        }
        return mapping;
    }

    private static List<EntityManagerRef> findEntityManagerMetaReferences(Class target)
    {
        List<EntityManagerRef> result = new ArrayList<EntityManagerRef>();

        BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();

        //TODO support other injection types
        Class currentParamClass = target;
        PersistenceContext persistenceContext;
        while (currentParamClass != null && !Object.class.getName().equals(currentParamClass.getName()))
        {
            //TODO scan methods to support a manual lookup

            for (Field currentField : currentParamClass.getDeclaredFields())
            {
                persistenceContext = currentField.getAnnotation(PersistenceContext.class);
                if (persistenceContext != null)
                {
                    result.add(new PersistenceContextMetaEntry(
                            currentParamClass,
                            currentField.getName(),
                            persistenceContext.unitName(),
                            PersistenceContextType.EXTENDED.equals(persistenceContext.type())));
                    continue;
                }

                if (EntityManager.class.isAssignableFrom(currentField.getType()))
                {
                    String key = createKey(currentField, beanManager);
                    //TODO discuss support of extended entity-managers
                    result.add(new EntityManagerRef(currentParamClass, currentField.getName(), key));
                }
            }
            currentParamClass = currentParamClass.getSuperclass();
        }

        return result;
    }

    private static String createKey(Field entityManagerField, BeanManager beanManager)
    {
        for (Annotation annotation : entityManagerField.getAnnotations())
        {
            if (beanManager.isQualifier(annotation.annotationType()))
            {
                //TODO add values of binding annotation-members
                //for now it's ok because we haven't allowed binding qualifier values here
                return annotation.annotationType().getName();
            }
        }

        return Default.class.getName();
    }

    private static ClassLoader getClassLoader()
    {
        return ClassUtils.getClassLoader(null);
    }
}
