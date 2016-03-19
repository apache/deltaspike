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

public class EntityDescriptor extends AbstractEntityDescriptor
{
    private String tableName;

    public EntityDescriptor()
    {
    }

    public EntityDescriptor(String[] id, String version, String name, Class<?> entityClass,
            Class<? extends Serializable> idClass, AbstractEntityDescriptor parent,
            String tableName)
    {
        super(id, version, name, entityClass, idClass, parent);
        this.tableName = tableName;
    }
    
    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("EntityDescriptor ")
                .append("[entityClass=").append(getEntityClass().getName())
                .append(", name=").append(getName())
                .append(", idClass=").append(getIdClass().getName())
                .append(", id=").append(getId())
                .append(", superClass=").append(getParent())
                .append(", tableName=").append(tableName)
                .append("]");
        return builder.toString();
    }
}
