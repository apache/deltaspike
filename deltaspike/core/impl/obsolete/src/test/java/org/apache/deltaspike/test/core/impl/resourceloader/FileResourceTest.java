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

import org.apache.deltaspike.core.api.resourceloader.InjectableResource;
import org.apache.deltaspike.core.api.resourceloader.FileResourceProvider;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class FileResourceTest
{
    private boolean created = false;
    @Before
    public void createTempFile()
    {
        if (!created)
        {
            File tmpDir = new File("target");
            try
            {
                File dest = new File(tmpDir,"/propsdsfileresource.properties");
                FileWriter fw = new FileWriter(dest);
                fw.write("some.propertykey=somevalue");
                fw.close();
                dest.deleteOnExit();
            }
            catch (IOException e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
            finally
            {
                created = true;
            }
        }
    }

    @Deployment
    public static Archive<?> createResourceLoaderArchive()
    {
        Archive<?> arch = ShrinkWrap.create(WebArchive.class, FileResourceTest.class.getSimpleName() + ".war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive());
        return arch;
    }

    @Test
    public void testInputStream(@InjectableResource(resourceProvider = FileResourceProvider.class,
            location="target/propsdsfileresource.properties")
            InputStream inputStream) throws IOException
    {
        Assert.assertNotNull(inputStream);
        Properties p = new Properties();
        p.load(inputStream);
        Assert.assertEquals("somevalue", p.getProperty("some.propertykey", "wrong answer"));
    }

    @Test
    public void testProperties(@InjectableResource(resourceProvider = FileResourceProvider.class,
            location="target/propsdsfileresource.properties")
            Properties properties)
    {
        Assert.assertEquals("somevalue", properties.getProperty("some.propertykey", "wrong answer"));
    }
}
