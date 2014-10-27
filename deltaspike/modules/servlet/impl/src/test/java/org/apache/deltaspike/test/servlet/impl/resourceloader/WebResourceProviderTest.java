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
package org.apache.deltaspike.test.servlet.impl.resourceloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.deltaspike.core.api.resourceloader.InjectableResource;
import org.apache.deltaspike.servlet.api.resourceloader.WebResourceProvider;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.servlet.impl.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class WebResourceProviderTest
{

    @Deployment
    public static WebArchive getDeployment()
    {
        return ShrinkWrap.create(WebArchive.class, WebResourceProviderTest.class.getSimpleName() + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndServletModuleArchive())
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml")
                .addAsWebResource(new StringAsset("foobar"), "foobar.txt")
                .addAsWebResource(new StringAsset("foobar"), "foo/bar.txt")
                .addAsWebResource(new StringAsset("foo=bar"), "foobar.properties");
    }

    @Inject
    @InjectableResource(location = "/foobar.txt", resourceProvider = WebResourceProvider.class)
    private InputStream streamAbsolutePath;

    @Inject
    @InjectableResource(location = "foobar.txt", resourceProvider = WebResourceProvider.class)
    private InputStream streamRelativePath;

    @Inject
    @InjectableResource(location = "/foo/bar.txt", resourceProvider = WebResourceProvider.class)
    private InputStream streamDirectory;

    @Inject
    @InjectableResource(location = "/foobar.properties", resourceProvider = WebResourceProvider.class)
    private Properties propertiesAbsolutePath;

    @Test
    public void testStreamWithAbsolutePath()
    {
        assertNotNull("Stream not injected", streamAbsolutePath);
    }

    @Test
    public void testStreamWithRelativePath()
    {
        assertNotNull("Stream not injected", streamRelativePath);
    }

    @Test
    public void testStreamWithFileInDirectory()
    {
        assertNotNull("Stream not injected", streamDirectory);
    }

    @Test
    public void testPropertiesWithAbsolutePath()
    {
        assertNotNull("Properties not injected", propertiesAbsolutePath);
        assertEquals("bar", propertiesAbsolutePath.get("foo"));
    }

}
