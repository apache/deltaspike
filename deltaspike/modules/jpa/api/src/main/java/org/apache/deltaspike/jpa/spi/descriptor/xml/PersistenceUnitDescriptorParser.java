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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.enterprise.inject.Typed;

@Typed
public class PersistenceUnitDescriptorParser extends DescriptorReader
{
    public static final String RESOURCE_PATH = "META-INF/persistence.xml";

    private final EntityMappingsDescriptorParser entityMappingsDescriptorParser
        = new EntityMappingsDescriptorParser();
    
    public List<PersistenceUnitDescriptor> readAll() throws IOException
    {
        List<PersistenceUnitDescriptor> result = new LinkedList<PersistenceUnitDescriptor>();
        List<Descriptor> persistenceXmls = readAllFromClassPath(RESOURCE_PATH);
        for (Descriptor desc : persistenceXmls)
        {
            result.addAll(lookupUnits(desc));
        }
        return Collections.unmodifiableList(result);
    }

    protected List<PersistenceUnitDescriptor> lookupUnits(Descriptor descriptor)
    {
        List<PersistenceUnitDescriptor> result = new LinkedList<PersistenceUnitDescriptor>();
        NodeList list = descriptor.getDocument().getDocumentElement().getElementsByTagName("persistence-unit");
        for (int i = 0; i < list.getLength(); i++)
        {
            Node node = list.item(i);

            String unitName = extractUnitName(node);
            String baseUrl = extractBaseUrl(descriptor.getUrl(), RESOURCE_PATH);
            List<EntityDescriptor> entities = extractMappings((Element) node, baseUrl, unitName);
            Map<String, String> properties = extractProperties((Element) node);

            result.add(new PersistenceUnitDescriptor(unitName, entities, properties));
        }
        return result;
    }

    protected List<EntityDescriptor> extractMappings(Element element, String baseUrl, String unitName)
    {
        try
        {
            List<EntityDescriptor> entities = new LinkedList<EntityDescriptor>();
            List<MappedSuperclassDescriptor> superClasses = new LinkedList<MappedSuperclassDescriptor>();
            NodeList list = element.getElementsByTagName("mapping-file");
            readMappingFiles(baseUrl, unitName, entities, superClasses, list);
            EntityMappingsDescriptor mappings = entityMappingsDescriptorParser.readDefaultOrm(baseUrl);
            entities.addAll(mappings.getEntityDescriptors());
            superClasses.addAll(mappings.getMappedSuperclassDescriptors());
            AbstractEntityHierarchyBuilder.buildHierarchy(entities, superClasses);
            return entities;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed initializing mapping files", e);
        }
    }

    protected void readMappingFiles(String baseUrl, String unitName,
                                  List<EntityDescriptor> entities, List<MappedSuperclassDescriptor> superClasses,
                                  NodeList list)
    {
        for (int i = 0; i < list.getLength(); i++)
        {
            String resource = list.item(i).getTextContent();
            try
            {
                EntityMappingsDescriptor mappings = entityMappingsDescriptorParser.readAll(baseUrl, resource);
                entities.addAll(mappings.getEntityDescriptors());
                superClasses.addAll(mappings.getMappedSuperclassDescriptors());
            }
            catch (Exception e)
            {
                throw new RuntimeException("[PersistenceUnit: " + unitName + "] " +
                        "Unable to resolve named mapping-file [" + resource + "]");
            }
        }
    }

    protected String extractUnitName(Node node)
    {
        return node.getAttributes().getNamedItem("name").getTextContent();
    }

    protected Map<String, String> extractProperties(Element element)
    {
        Map<String, String> propertiesMap = new HashMap<String, String>();

        Node propertiesNode = element.getElementsByTagName("properties").item(0);
        if (propertiesNode != null)
        {
            NodeList propertyNodes = propertiesNode.getChildNodes();
            for (int i = 0; i < propertyNodes.getLength(); i++)
            {
                if ("property".equals(propertyNodes.item(i).getNodeName()))
                {
                    Element propertyNode = (Element) propertyNodes.item(i);
                    propertiesMap.put(propertyNode.getAttribute("name"), propertyNode.getAttribute("value"));
                }
            }
        }

        return Collections.unmodifiableMap(propertiesMap);
    }
}
