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

import static java.lang.Thread.currentThread;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

abstract class DescriptorReader
{

    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    List<Descriptor> readAllFromClassPath(String resource) throws IOException
    {
        List<Descriptor> result = new LinkedList<Descriptor>();
        Enumeration<URL> urls = classLoader().getResources(resource);
        while (urls.hasMoreElements())
        {
            URL u = urls.nextElement();
            result.add(readFromUrl(u));
        }
        return Collections.unmodifiableList(result);
    }

    Descriptor readFromClassPath(String resource) throws IOException
    {
        return readFromUrl(classLoader().getResource(resource));
    }

    Descriptor readFromUrl(URL url) throws IOException
    {
        if (!exists(url))
        {
            throw new IllegalArgumentException("URL does not exist: " + url);
        }
        InputStream stream = url.openStream();
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return new Descriptor(builder.parse(new InputSource(stream)), url);
        }
        catch (SAXException e)
        {
            throw new RuntimeException("Failed reading XML document", e);
        }
        catch (ParserConfigurationException e)
        {
            throw new RuntimeException("Failed reading XML document", e);
        }
        finally
        {
            stream.close();
        }
    }

    Descriptor read(String baseUrl, String resource) throws IOException
    {
        try
        {
            URL url = new URL(baseUrl + resource);
            return readFromUrl(url);
        }
        catch (IllegalArgumentException e)
        {
            return readFromClassPath(resource);
        }
    }

    String extractBaseUrl(URL fileUrl, String resource)
    {
        String file = fileUrl.toString();
        return file.substring(0, file.length() - resource.length());
    }

    ClassLoader classLoader()
    {
        return currentThread().getContextClassLoader();
    }

    boolean exists(URL url)
    {
        try
        {
            return url != null && url.openConnection() != null && url.openConnection().getContentLength() > 0;
        }
        catch (IOException e)
        {
            return false;
        }
    }

}
