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

import java.util.List;
import java.util.Map;

public class PersistenceUnit
{

    public static final String RESOURCE_PATH = "META-INF/persistence.xml";
    public static final String DEFAULT_ORM_PATH = "META-INF/orm.xml";

    private final String unitName;
    private final List<EntityDescriptor> entities;
    private final Map<String, String> properties;

    PersistenceUnit(String unitName, List<EntityDescriptor> entities, Map<String, String> properties)
    {
        this.unitName = unitName;
        this.entities = entities;
        this.properties = properties;
    }

    public EntityDescriptor find(Class<?> entityClass)
    {
        for (EntityDescriptor entity : entities)
        {
            if (entity.is(entityClass))
            {
                return entity;
            }
        }
        return null;
    }

    public String getUnitName()
    {
        return unitName;
    }
    
    public Map<String, String> getProperties()
    {
        return properties;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("PersistenceUnit [unitName=").append(unitName)
                .append(", entities=").append(entities).append("]");
        return builder.toString();
    }

}
