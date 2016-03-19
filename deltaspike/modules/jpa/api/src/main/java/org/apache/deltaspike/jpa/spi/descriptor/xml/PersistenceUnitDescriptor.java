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
import java.util.Map;

public class PersistenceUnitDescriptor
{
    private String name;
    /*
    private String transactionType;
    private boolean excludeUnlistedClasses;
    */
    private List<EntityDescriptor> entityDescriptors;
    private Map<String, String> properties;

    public PersistenceUnitDescriptor(String name, List<EntityDescriptor> entityDescriptors,
            Map<String, String> properties)
    {
        this.name = name;
        this.entityDescriptors = entityDescriptors;
        this.properties = properties;
    }
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<EntityDescriptor> getEntityDescriptors()
    {
        return entityDescriptors;
    }

    public void setEntityDescriptors(List<EntityDescriptor> entityDescriptors)
    {
        this.entityDescriptors = entityDescriptors;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("PersistenceUnit [name=").append(name)
                .append(", entityDescriptors=").append(entityDescriptors).append("]");
        return builder.toString();
    }
}
