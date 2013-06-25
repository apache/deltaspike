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

import static org.apache.deltaspike.data.impl.util.QueryUtils.isEmpty;

import java.io.Serializable;

class EntityDescriptor extends PersistentClassDescriptor
{

    EntityDescriptor(String name, String packageName, String className, String idClass, String id)
    {
        super(name, packageName, className, idClass, id);
    }

    public boolean is(Class<?> entityClass)
    {
        return this.entityClass.equals(entityClass);
    }

    @Override
    public Class<? extends Serializable> getIdClass()
    {
        if (idClass == null && getParent() != null)
        {
            return getParent().getIdClass();
        }
        return super.getIdClass();
    }

    @Override
    public String getId()
    {
        if (isEmpty(id) && getParent() != null)
        {
            return getParent().getId();
        }
        return super.getId();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("EntityDescriptor ")
                .append("[entityClass=").append(className(entityClass))
                .append(", name=").append(name)
                .append(", idClass=").append(className(idClass))
                .append(", id=").append(id)
                .append(", superClass=").append(getParent())
                .append("]");
        return builder.toString();
    }

}
