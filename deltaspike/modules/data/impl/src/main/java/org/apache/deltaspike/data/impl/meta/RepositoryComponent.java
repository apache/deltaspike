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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.FlushModeType;

import org.apache.deltaspike.data.api.EntityManagerConfig;
import org.apache.deltaspike.data.api.EntityManagerResolver;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.util.EntityUtils;

/**
 * Stores information about a specific Repository. Extracts information about:
 * <ul>
 * <li>The Repository class</li>
 * <li>The target entity the Repository is for</li>
 * <li>The primary key class</li>
 * <li>All methods of the Repository.</li>
 * </ul>
 *
 * @author thomashug
 */
public class RepositoryComponent
{

    private static final Logger log = Logger.getLogger(RepositoryComponent.class.getName());

    private final Class<?> repoClass;
    private final RepositoryEntity entityClass;
    private final Class<? extends EntityManagerResolver> entityManagerResolver;
    private final FlushModeType entityManagerFlushMode;

    private final Map<Method, RepositoryMethod> methods = new HashMap<Method, RepositoryMethod>();

    public RepositoryComponent(Class<?> repoClass, RepositoryEntity entityClass)
    {
        if (entityClass == null)
        {
            throw new IllegalArgumentException("Entity class cannot be null");
        }
        this.repoClass = repoClass;
        this.entityClass = entityClass;
        this.entityManagerResolver = extractEntityManagerResolver(repoClass);
        this.entityManagerFlushMode = extractEntityManagerFlushMode(repoClass);
        initialize();
    }

    public String getEntityName()
    {
        return EntityUtils.entityName(entityClass.getEntityClass());
    }

    /**
     * Looks up method meta data by a Method object.
     *
     * @param method    The Repository method.
     * @return Method meta data.
     */
    public RepositoryMethod lookupMethod(Method method)
    {
        return methods.get(method);
    }

    /**
     * Looks up the method type by a Method object.
     *
     * @param method    The Repository method.
     * @return Method meta data.
     */
    public MethodType lookupMethodType(Method method)
    {
        return lookupMethod(method).getMethodType();
    }

    /**
     * Gets the entity class related the Repository.
     *
     * @return The class of the entity related to the Repository.
     */
    public Class<?> getEntityClass()
    {
        return entityClass.getEntityClass();
    }

    /**
     * Gets the entity primary key class related the Repository.
     *
     * @return The class of the entity primary key related to the Repository.
     */
    public Class<? extends Serializable> getPrimaryKey()
    {
        return entityClass.getPrimaryClass();
    }

    /**
     * Returns the original Repository class this meta data is related to.
     *
     * @return The class of the Repository.
     */
    public Class<?> getRepositoryClass()
    {
        return repoClass;
    }

    public String getMethodPrefix()
    {
        return repoClass.getAnnotation(Repository.class).methodPrefix();
    }

    public boolean hasEntityManagerResolver()
    {
        return getEntityManagerResolverClass() != null;
    }

    public Class<? extends EntityManagerResolver> getEntityManagerResolverClass()
    {
        return entityManagerResolver;
    }

    public FlushModeType getEntityManagerFlushMode()
    {
        return entityManagerFlushMode;
    }

    private void initialize()
    {
        Collection<Class<?>> allImplemented = collectClasses();
        for (Class<?> implemented : allImplemented)
        {
            Method[] repoClassMethods = implemented.getDeclaredMethods();
            for (Method repoClassMethod : repoClassMethods)
            {
                RepositoryMethod repoMethod = new RepositoryMethod(repoClassMethod, this);
                methods.put(repoClassMethod, repoMethod);
            }
        }
    }

    private Set<Class<?>> collectClasses()
    {
        Set<Class<?>> result = new HashSet<Class<?>>();
        Class<?> current = repoClass;
        while (!Object.class.equals(current) && current != null)
        {
            result.add(current);
            Class<?>[] interfaces = current.getInterfaces();
            if (interfaces != null)
            {
                result.addAll(Arrays.asList(interfaces));
            }
            current = current.getSuperclass();
        }
        log.log(Level.FINER, "collectClasses(): Found {0} for {1}", new Object[] { result, repoClass });
        return result;
    }

    private Class<? extends EntityManagerResolver> extractEntityManagerResolver(Class<?> clazz)
    {
        EntityManagerConfig config = extractEntityManagerConfig(clazz);
        if (config != null && !EntityManagerResolver.class.equals(config.entityManagerResolver()))
        {
            return config.entityManagerResolver();
        }
        return null;
    }

    private FlushModeType extractEntityManagerFlushMode(Class<?> clazz)
    {
        EntityManagerConfig config = extractEntityManagerConfig(clazz);
        if (config != null)
        {
            return config.flushMode();
        }
        return null;
    }

    private EntityManagerConfig extractEntityManagerConfig(Class<?> clazz)
    {
        if (clazz.isAnnotationPresent(EntityManagerConfig.class))
        {
            return clazz.getAnnotation(EntityManagerConfig.class);
        }
        return null;
    }

}
