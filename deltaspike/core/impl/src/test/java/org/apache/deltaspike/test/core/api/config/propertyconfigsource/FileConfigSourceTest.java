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
package org.apache.deltaspike.test.core.api.config.propertyconfigsource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.PropertyFileConfig;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Test for picking up a file system based config
 */
@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class FileConfigSourceTest
{
    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "FileConfigSourceTest.jar")
            .addClasses(FileConfigSourceTest.class, FileSystemConfig.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, "beanProvider.war")
            .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
            .addAsLibraries(testJar)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }


    @Test
    public void testConfig() {
        String val = ConfigResolver.getPropertyValue("deltaspike.test.config.from.file");
        Assert.assertNotNull(val);
        Assert.assertEquals("it works", val);
    }

    public static class FileSystemConfig implements PropertyFileConfig
    {
        private final String configFileLocation;

        /**
         * This ct is actually only a hack to create a temporary file on the target system
         * With exactly the content we will later look up.
         */
        public FileSystemConfig()
        {
            try
            {
                File tempFile = File.createTempFile("deltaspike", ".properties");
                FileWriter fw = new FileWriter(tempFile);
                fw.write("deltaspike.test.config.from.file=it works");
                fw.close();
                configFileLocation = tempFile.toURI().toURL().toExternalForm();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String getPropertyFileName()
        {
            return configFileLocation;
        }

        @Override
        public boolean isOptional()
        {
            return false;
        }
    }
}
