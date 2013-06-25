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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EntityDescriptorReader extends DescriptorReader
{

    public MappingFile readAll(String baseUrl, String resource) throws IOException
    {
        return readFromDocument(read(baseUrl, resource).getDocument());
    }

    public MappingFile readDefaultOrm(String baseUrl) throws IOException
    {
        try
        {
            Descriptor desc = read(baseUrl, PersistenceUnit.DEFAULT_ORM_PATH);
            return readFromDocument(desc.getDocument());
        }
        catch (IllegalArgumentException e)
        {
            return new MappingFile(Collections.<EntityDescriptor> emptyList(),
                    Collections.<MappedSuperclassDescriptor> emptyList());
        }
    }

    public MappingFile readFromDocument(Document doc)
    {
        List<EntityDescriptor> entities = new EntityBuilder<EntityDescriptor>()
        {
            @Override
            EntityDescriptor instance(String name, String packageName, String className,
                    String idClass, String id)
            {
                return new EntityDescriptor(name, packageName, className, idClass, id);
            }

            @Override
            String tagName()
            {
                return "entity";
            }
        }
                .build(doc);
        List<MappedSuperclassDescriptor> superClasses = new EntityBuilder<MappedSuperclassDescriptor>()
        {
            @Override
            MappedSuperclassDescriptor instance(String name, String packageName, String className,
                    String idClass, String id)
            {
                return new MappedSuperclassDescriptor(name, packageName, className, idClass, id);
            }

            @Override
            String tagName()
            {
                return "mapped-superclass";
            }
        }
                .build(doc);
        return new MappingFile(entities, superClasses);
    }

    private String extractNodeAttribute(Element element, String childName, String attribute)
    {
        NodeList list = element.getElementsByTagName(childName);
        if (list.getLength() == 0)
        {
            return null;
        }
        return extractAttribute(list.item(0), attribute);
    }

    private String extractAttribute(Node item, String name)
    {
        Node node = item.getAttributes().getNamedItem(name);
        if (node != null)
        {
            return node.getTextContent();
        }
        return null;
    }

    private String extractNodeContent(Element element, String name)
    {
        NodeList list = element.getElementsByTagName(name);
        if (list.getLength() == 0)
        {
            return null;
        }
        return list.item(0).getTextContent();
    }

    public static class MappingFile
    {
        private final List<EntityDescriptor> entities;
        private final List<MappedSuperclassDescriptor> superClasses;

        public MappingFile(List<EntityDescriptor> entities, List<MappedSuperclassDescriptor> superClasses)
        {
            this.entities = entities;
            this.superClasses = superClasses;
        }

        public List<EntityDescriptor> getEntities()
        {
            return entities;
        }

        public List<MappedSuperclassDescriptor> getSuperClasses()
        {
            return superClasses;
        }
    }

    private abstract class EntityBuilder<T extends PersistentClassDescriptor>
    {

        public List<T> build(Document doc)
        {
            List<T> result = new LinkedList<T>();
            String packageName = extractNodeContent(doc.getDocumentElement(), "package");
            NodeList mappings = doc.getElementsByTagName(tagName());
            for (int i = 0; i < mappings.getLength(); i++)
            {
                String name = extractAttribute(mappings.item(i), "name");
                String className = extractAttribute(mappings.item(i), "class");
                String idClass = extractNodeAttribute((Element) mappings.item(i), "id-class", "class");
                String id = extractNodeAttribute((Element) mappings.item(i), "id", "name");
                String embeddedId = extractNodeAttribute((Element) mappings.item(i), "embedded-id", "name");
                result.add(instance(name, packageName, className, idClass, id != null ? id : embeddedId));
            }
            return result;
        }

        abstract T instance(String name, String packageName, String className, String idClass, String id);

        abstract String tagName();

    }
}
