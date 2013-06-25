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

public final class DescriptorHierarchyBuilder
{

    private final List<EntityDescriptor> entities;
    private final List<MappedSuperclassDescriptor> superClasses;

    private DescriptorHierarchyBuilder(List<EntityDescriptor> entities,
            List<MappedSuperclassDescriptor> superClasses)
    {
        this.entities = entities;
        this.superClasses = superClasses;
    }

    public static DescriptorHierarchyBuilder newInstance(List<EntityDescriptor> entities,
            List<MappedSuperclassDescriptor> superClasses)
    {
        return new DescriptorHierarchyBuilder(entities, superClasses);
    }

    public void buildHierarchy()
    {
        for (EntityDescriptor descriptor : entities)
        {
            buildHierarchy(descriptor);
        }
    }

    private void buildHierarchy(PersistentClassDescriptor descriptor)
    {
        Class<?> superClass = descriptor.getEntityClass().getSuperclass();
        while (superClass != null)
        {
            PersistentClassDescriptor superDescriptor = findPersistentClassDescriptor(superClass);
            if (superDescriptor != null)
            {
                if (descriptor.getParent() == null)
                {
                    buildHierarchy(superDescriptor);
                }
                descriptor.setParent(superDescriptor);
                return;
            }
            superClass = superClass.getSuperclass();
        }
    }

    private PersistentClassDescriptor findPersistentClassDescriptor(Class<?> superClass)
    {
        for (MappedSuperclassDescriptor descriptor : superClasses)
        {
            if (descriptor.getEntityClass().equals(superClass))
            {
                return descriptor;
            }
        }
        for (EntityDescriptor descriptor : entities)
        {
            if (descriptor.getEntityClass().equals(superClass))
            {
                return descriptor;
            }
        }
        return null;
    }

}
