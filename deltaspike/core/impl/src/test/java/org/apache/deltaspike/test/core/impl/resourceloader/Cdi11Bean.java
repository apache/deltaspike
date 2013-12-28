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
package org.apache.deltaspike.test.core.impl.resourceloader;

import org.apache.deltaspike.core.api.resourceloader.ClasspathStorage;
import org.apache.deltaspike.core.api.resourceloader.ExternalResource;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

@Dependent
public class Cdi11Bean implements TestResourceHolder
{
    @Inject
    @ExternalResource(storage = ClasspathStorage.class,location="testconfig.properties")
    private InputStream inputStream;

    @Inject
    @ExternalResource(storage = ClasspathStorage.class,location="testconfig.properties")
    private Properties properties;

    @Inject
    @ExternalResource(storage = ClasspathStorage.class,location="META-INF/beans.xml")
    private List<InputStream> inputStreams;

    @Inject
    @Any
    private Instance<InputStream> inputStreamInstance;

    @Inject
    @Any
    private Instance<Properties> propertiesInstance;

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public Properties getProperties()
    {
        return properties;
    }

    public Instance<InputStream> getInputStreamInstance()
    {
        return inputStreamInstance;
    }

    public Instance<Properties> getPropertiesInstance()
    {
        return propertiesInstance;
    }

    @Override
    public List<InputStream> getInputStreams() {
        return inputStreams;
    }
}
