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

public class EntityMappingsDescriptor
{
    private List<MappedSuperclassDescriptor> mappedSuperclassDescriptors;
    private List<EntityDescriptor> entityDescriptors;
    private String packageName;

    public EntityMappingsDescriptor(List<MappedSuperclassDescriptor> mappedSuperclassDescriptors,
            List<EntityDescriptor> entityDescriptors, String packageName)
    {
        this.mappedSuperclassDescriptors = mappedSuperclassDescriptors;
        this.entityDescriptors = entityDescriptors;
        this.packageName = packageName;
    }
    
    public List<MappedSuperclassDescriptor> getMappedSuperclassDescriptors()
    {
        return mappedSuperclassDescriptors;
    }

    public void setMappedSuperclassDescriptors(List<MappedSuperclassDescriptor> mappedSuperclassDescriptors)
    {
        this.mappedSuperclassDescriptors = mappedSuperclassDescriptors;
    }

    public List<EntityDescriptor> getEntityDescriptors()
    {
        return entityDescriptors;
    }

    public void setEntityDescriptors(List<EntityDescriptor> entityDescriptors)
    {
        this.entityDescriptors = entityDescriptors;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }
}
