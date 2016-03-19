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

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.enterprise.inject.Typed;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Typed
public class EntityMappingsDescriptorParser extends DescriptorReader
{
    public static final String DEFAULT_ORM_PATH = "META-INF/orm.xml";

    public EntityMappingsDescriptor readAll(String baseUrl, String resource) throws IOException
    {
        Document document = read(baseUrl, resource).getDocument();
        return readFromDocument(document);
    }

    public EntityMappingsDescriptor readDefaultOrm(String baseUrl) throws IOException
    {
        try
        {
            Descriptor desc = read(baseUrl, DEFAULT_ORM_PATH);
            return readFromDocument(desc.getDocument());
        }
        catch (Exception e)
        {
            return new EntityMappingsDescriptor(Collections.<MappedSuperclassDescriptor>emptyList(),
                    Collections.<EntityDescriptor>emptyList(), null);
        }
    }

    protected EntityMappingsDescriptor readFromDocument(Document doc)
    {
        String packageName =
            parsePackageName(doc);
        List<MappedSuperclassDescriptor> mappedSuperclassDescriptors =
            parseMappedSuperclassDescriptors(doc, packageName);
        List<EntityDescriptor> entityDescriptors =
            parseEntityDescriptors(doc, packageName);
        
        return new EntityMappingsDescriptor(mappedSuperclassDescriptors, entityDescriptors, packageName);
    }

    protected String extractNodeAttribute(Element element, String childName, String attribute)
    {
        NodeList list = element.getElementsByTagName(childName);
        if (list.getLength() == 0)
        {
            return null;
        }
        return extractAttribute(list.item(0), attribute);
    }

    protected String extractAttribute(Node item, String name)
    {
        Node node = item.getAttributes().getNamedItem(name);
        if (node != null)
        {
            return node.getTextContent();
        }
        return null;
    }
    
    protected String[] extractNodeAttributes(Element element, String childName, String attribute)
    {
        NodeList list = element.getElementsByTagName(childName);
        if (list.getLength() == 0)
        {
            return null;
        }
        return extractAttributes(list, attribute);
    }
    
    protected String[] extractAttributes(NodeList list, String name)
    {
        String[] values = null;
        
        for (int i = 0; i < list.getLength(); i++)
        {
            Node node = list.item(i).getAttributes().getNamedItem(name);
            if (node != null)
            {
                if (values == null)
                {
                    values = new String[list.getLength()];
                }
                
                values[i] = node.getTextContent();
            }
        }

        return values;
    }

    protected String extractNodeContent(Element element, String name)
    {
        NodeList list = element.getElementsByTagName(name);
        if (list.getLength() == 0)
        {
            return null;
        }
        return list.item(0).getTextContent();
    }

    protected String parsePackageName(Document doc)
    {
        return extractNodeContent(doc.getDocumentElement(), "package");
    }
    
    protected List<MappedSuperclassDescriptor> parseMappedSuperclassDescriptors(Document doc, String packageName)
    {
        List<MappedSuperclassDescriptor> result = new LinkedList<MappedSuperclassDescriptor>();

        NodeList mappings = doc.getElementsByTagName("mapped-superclass");
        for (int i = 0; i < mappings.getLength(); i++)
        {
            Node node = mappings.item(i);
            
            MappedSuperclassDescriptor entityDescriptor = new MappedSuperclassDescriptor();
            parseCommonEntityDescriptorAttributes(packageName, entityDescriptor, node);
            
            result.add(entityDescriptor);
        }
        
        return result;
    }
    
    protected List<EntityDescriptor> parseEntityDescriptors(Document doc, String packageName)
    {
        List<EntityDescriptor> result = new LinkedList<EntityDescriptor>();

        NodeList mappings = doc.getElementsByTagName("entity");
        for (int i = 0; i < mappings.getLength(); i++)
        {
            Node node = mappings.item(i);
            
            EntityDescriptor entityDescriptor = new EntityDescriptor();
            parseCommonEntityDescriptorAttributes(packageName, entityDescriptor, node);
            entityDescriptor.setTableName(extractNodeAttribute((Element) node, "table", "name"));

            result.add(entityDescriptor);
        }
        
        return result;
    } 
    
    protected void parseCommonEntityDescriptorAttributes(String packageName,
            AbstractEntityDescriptor entityDescriptor, Node node)
    {
        entityDescriptor.setName(extractAttribute(node, "name"));
        entityDescriptor.setVersion(extractNodeAttribute((Element) node, "version", "name"));

        String[] id = extractNodeAttributes((Element) node, "id", "name");
        if (id != null)
        {
            entityDescriptor.setId(id);
        }
        else
        {
            String embeddedId = extractNodeAttribute((Element) node, "embedded-id", "name");
            if (embeddedId != null)
            {
                entityDescriptor.setId(new String[] { embeddedId });
            }
        }

        String className = extractAttribute(node, "class");
        if (className != null)
        {
            try
            {
                entityDescriptor.setEntityClass(Class.forName(buildClassName(className, packageName)));
            }
            catch (ClassNotFoundException e)
            {
                throw new IllegalArgumentException("Can't get class " + buildClassName(className, packageName), e);
            }
        }

        String idClass = extractNodeAttribute((Element) node, "id-class", "class");
        if (idClass != null)
        {
            try
            {
                entityDescriptor.setIdClass(
                        (Class<? extends Serializable>) Class.forName(buildClassName(idClass, packageName)));
            }
            catch (ClassNotFoundException e)
            {
                throw new IllegalArgumentException("Can't get class " + buildClassName(className, packageName), e);
            }
        }
    }
    
    protected String buildClassName(String clazzName, String packageName)
    {
        if (clazzName == null && packageName == null)
        {
            return null;
        }
        return (packageName != null && !isClassNameQualified(clazzName)) ? packageName + "." + clazzName : clazzName;
    }

    protected boolean isClassNameQualified(String name)
    {
        return name.contains(".");
    }

}
