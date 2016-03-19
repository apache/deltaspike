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

import java.util.List;

public final class AbstractEntityHierarchyBuilder
{
    private AbstractEntityHierarchyBuilder()
    {
    }

    public static void buildHierarchy(List<EntityDescriptor> entities, List<MappedSuperclassDescriptor> superClasses)
    {
        for (EntityDescriptor descriptor : entities)
        {
            buildHierarchy(descriptor, entities, superClasses);
        }
    }

    protected static void buildHierarchy(AbstractEntityDescriptor descriptor,
            List<EntityDescriptor> entities, List<MappedSuperclassDescriptor> superClasses)
    {
        Class<?> superClass = descriptor.getEntityClass().getSuperclass();
        while (superClass != null)
        {
            AbstractEntityDescriptor superDescriptor =
                    findPersistentClassDescriptor(superClass, entities, superClasses);
            if (superDescriptor != null)
            {
                if (descriptor.getParent() == null)
                {
                    buildHierarchy(superDescriptor, entities, superClasses);
                }

                descriptor.setParent(superDescriptor);
                return;
            }

            superClass = superClass.getSuperclass();
        }
    }

    protected static AbstractEntityDescriptor findPersistentClassDescriptor(Class<?> superClass,
            List<EntityDescriptor> entities, List<MappedSuperclassDescriptor> superClasses)
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
