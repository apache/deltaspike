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

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleQueryInOutMapperBase<Entity, Dto> implements QueryInOutMapper<Entity>
{
    protected abstract Dto toDto(Entity entity);
    protected abstract Entity toEntity(Dto dto);

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
        final List<Object> mapped = new ArrayList<Object>(result.size());
        if (result != null)
        {
            for (final Entity a : result)
            {
                mapped.add(mapResult(a));
            }
        }
        return mapped;
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
        return toEntity((Dto) parameter);
    }
}
