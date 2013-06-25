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

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.deltaspike.data.impl.meta.unit.EntityDescriptorReader.MappingFile;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PersistenceUnitReader extends DescriptorReader
{

    public List<PersistenceUnit> readAll() throws IOException
    {
        List<PersistenceUnit> result = new LinkedList<PersistenceUnit>();
        List<Descriptor> persistenceXmls = readAllFromClassPath(PersistenceUnit.RESOURCE_PATH);
        for (Descriptor desc : persistenceXmls)
        {
            result.addAll(lookupUnits(desc));
        }
        return Collections.unmodifiableList(result);
    }

    private List<PersistenceUnit> lookupUnits(Descriptor descriptor)
    {
        List<PersistenceUnit> result = new LinkedList<PersistenceUnit>();
        NodeList list = descriptor.getDocument().getDocumentElement().getElementsByTagName("persistence-unit");
        for (int i = 0; i < list.getLength(); i++)
        {
            Node node = list.item(i);
            String unitName = extractUnitName(node);
            String baseUrl = extractBaseUrl(descriptor.getUrl(), PersistenceUnit.RESOURCE_PATH);
            List<EntityDescriptor> entities = extractMappings((Element) node, baseUrl);
            result.add(new PersistenceUnit(unitName, entities));
        }
        return result;
    }

    private List<EntityDescriptor> extractMappings(Element element, String baseUrl)
    {
        try
        {
            EntityDescriptorReader reader = new EntityDescriptorReader();
            List<EntityDescriptor> entities = new LinkedList<EntityDescriptor>();
            List<MappedSuperclassDescriptor> superClasses = new LinkedList<MappedSuperclassDescriptor>();
            NodeList list = element.getElementsByTagName("mapping-file");
            for (int i = 0; i < list.getLength(); i++)
            {
                MappingFile mappings = reader.readAll(baseUrl, list.item(i).getTextContent());
                entities.addAll(mappings.getEntities());
                superClasses.addAll(mappings.getSuperClasses());
            }
            MappingFile mappings = reader.readDefaultOrm(baseUrl);
            entities.addAll(mappings.getEntities());
            superClasses.addAll(mappings.getSuperClasses());
            DescriptorHierarchyBuilder.newInstance(entities, superClasses).buildHierarchy();
            return entities;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed initializing mapping files", e);
        }
    }

    private String extractUnitName(Node node)
    {
        return node.getAttributes().getNamedItem("name").getTextContent();
    }

}
