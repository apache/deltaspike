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
        catch (Exception e)
        {
            return new MappingFile(Collections.<EntityDescriptor>emptyList(),
                    Collections.<MappedSuperclassDescriptor>emptyList());
        }
    }

    public MappingFile readFromDocument(Document doc)
    {
        List<EntityDescriptor> entities = new EntityBuilder<EntityDescriptor>()
        {
            @Override
            protected EntityDescriptor instance(String name, String packageName, String className,
                                      String idClass, String id, String version, String tableName)
            {
                return new EntityDescriptor(name, packageName, className, idClass, id, version, tableName);
            }

            @Override
            protected String tagName()
            {
                return "entity";
            }
        }
                .build(doc);
        List<MappedSuperclassDescriptor> superClasses = new MappedSuperClassBuilder<MappedSuperclassDescriptor>()
        {
            @Override
            protected MappedSuperclassDescriptor instance(String name, String packageName, String className,
                                                String idClass, String id, String version)
            {
                return new MappedSuperclassDescriptor(name, packageName, className, idClass, id, version);
            }

            @Override
            protected String tagName()
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


    private abstract class PersistenceBuilder<T extends PersistentClassDescriptor>
    {
        protected List<T> result;
        protected String packageName;
        protected String name;
        protected String className;
        protected String idClass;
        protected String id;
        protected String version;
        protected String embeddedId;

        public List<T> build(Document doc)
        {
            this.result = new LinkedList<T>();
            this.packageName = extractNodeContent(doc.getDocumentElement(), "package");
            NodeList mappings = doc.getElementsByTagName(tagName());
            for (int i = 0; i < mappings.getLength(); i++)
            {
                this.name = extractAttribute(mappings.item(i), "name");
                this.className = extractAttribute(mappings.item(i), "class");
                this.idClass = extractNodeAttribute((Element) mappings.item(i), "id-class", "class");
                this.id = extractNodeAttribute((Element) mappings.item(i), "id", "name");
                this.version = extractNodeAttribute((Element) mappings.item(i), "version", "name");
                this.embeddedId = extractNodeAttribute((Element) mappings.item(i), "embedded-id", "name");
                addFields((Element) mappings.item(i));
                addInResult();
            }
            return this.result;
        }

        protected abstract String tagName();

        protected abstract void addInResult();

        protected abstract void addFields(Element element);
    }

    private abstract class MappedSuperClassBuilder<T extends PersistentClassDescriptor> extends PersistenceBuilder
    {
        protected abstract T instance(String name, String packageName, String className, String idClass, String id,
                                      String version);

        protected abstract String tagName();

        @Override
        protected void addInResult()
        {
            result.add(instance(name, packageName, className, idClass, id != null ? id : embeddedId, version));
        }

        @Override
        protected void addFields(Element element)
        {
            // do nothing;
        }
    }

    private abstract class EntityBuilder<T extends PersistentClassDescriptor> extends PersistenceBuilder
    {

        protected String tableName;

        protected abstract T instance(String name, String packageName, String className, String idClass, String id,
                                      String version, String tableName);

        protected abstract String tagName();

        @Override
        protected void addInResult()
        {
            result.add(instance(name, packageName, className, idClass, id != null ? id : embeddedId,
                version, tableName));
        }

        @Override
        protected void addFields(Element element)
        {
            this.tableName = extractNodeAttribute(element, "table", "name");
        }
    }
}
