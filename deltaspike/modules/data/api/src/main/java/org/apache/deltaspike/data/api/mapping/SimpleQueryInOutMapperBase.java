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
package org.apache.deltaspike.data.api.mapping;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.deltaspike.data.spi.QueryInvocationContext;

/**
 * A base mapper to map from Dto to Entity and vice versa. This should be sufficient
 * for most mapping cases and simplify the implementation of a mapper.
 *
 * @param <Entity>      The Entity type.
 * @param <Dto>         The Dto type.
 */
public abstract class SimpleQueryInOutMapperBase<Entity, Dto> implements QueryInOutMapper<Entity>
{
    @Inject
    private QueryInvocationContext context;

    /**
     * Return the primary key of the Entity corresponding to the Dto. If this is a new
     * Entity, return {@code null}.
     * @param dto       The Dto to map to an Entity.
     * @return          The Entity primary key, or {@code null} for a new Entity.
     */
    protected abstract Object getPrimaryKey(Dto dto);

    protected abstract Dto toDto(Entity entity);

    /**
     * Map a Dto to an Entity. In case the Dto contains a valid primary key,
     * the Entity will be retrieved first and used as method parameter. Otherwise
     * Entity is a unmanaged new instance.
     *
     * @param entity    Either a managed Entity looked up by the primary key,
     *                  or a new Entity instance.
     * @param dto       The Dto to map.
     * @return          Mapped Entity.
     */
    protected abstract Entity toEntity(Entity entity, Dto dto);

    @Override
    public Object mapResult(final Entity result)
    {
        if (result == null)
        {
            return null;
        }
        return toDto(result);
    }

    @Override
    public Object mapResultList(final List<Entity> result)
    {
        if (result != null)
        {
            final List<Object> mapped = new ArrayList<Object>(result.size());
            for (final Entity a : result)
            {
                mapped.add(mapResult(a));
            }
            return mapped;
        }
        return new ArrayList<Object>();
    }

    @Override
    public boolean mapsParameter(final Object parameter)
    {
        if (parameter == null)
        {
            return false;
        }
        final String name = parameter.getClass().getName();
        return Object.class.isInstance(parameter) && !(name.startsWith("java.") || name.startsWith("javax."));
    }

    @Override
    public Object mapParameter(final Object parameter)
    {
        Dto dto = (Dto) parameter;
        Object primaryKey = getPrimaryKey(dto);
        if (primaryKey != null)
        {
            Entity entity = findEntity(primaryKey);
            return toEntity(entity, dto);
        }
        return toEntity(newEntity(), dto);
    }

    @SuppressWarnings("unchecked")
    protected Entity newEntity()
    {
        try
        {
            Class<?> entityClass = context.getEntityClass();
            Constructor<?> constructor = entityClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (Entity) constructor.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed instantiating new Entity", e);
        }
    }

    @SuppressWarnings("unchecked")
    protected Entity findEntity(Object primaryKey)
    {
        return (Entity) context.getEntityManager().find(context.getEntityClass(), primaryKey);
    }
}
