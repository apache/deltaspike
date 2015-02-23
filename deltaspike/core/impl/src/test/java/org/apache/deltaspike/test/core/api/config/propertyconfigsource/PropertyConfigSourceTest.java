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

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.junit.Test;

@RunWith(Arquillian.class)
@Category(SeCategory.class) //X TODO this is only SeCategory as there is currently an Arq problem with properties!
public class PropertyConfigSourceTest
{
    private final static String CONFIG_FILE_NAME = "myconfig.properties";
    private final static String BOOTCONFIG_FILE_NAME = "myboottimeconfig.properties";
    private final static String NOT_PICKED_UP_CONFIG_FILE_NAME = "mynotpickedupconfig.properties";

    /**
     *X TODO creating a WebArchive is only a workaround because JavaArchive cannot contain other archives.
     */
    @Deployment(name = "tomee")
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "PropertyConfigSourceTest.jar")
                .addPackage(PropertyConfigSourceTest.class.getPackage())
                .addAsResource(CONFIG_FILE_NAME)
                .addAsResource(BOOTCONFIG_FILE_NAME)
                .addAsResource(NOT_PICKED_UP_CONFIG_FILE_NAME)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, "beanProvider.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }


    @Test
    public void testCustomPropertyConfigSources() throws Exception
    {
        Assert.assertTrue(
                Thread.currentThread().getContextClassLoader().getResources(CONFIG_FILE_NAME).hasMoreElements());

        String value = ConfigResolver.getPropertyValue("some.propertykey");
        Assert.assertNotNull(value);
        Assert.assertEquals("somevalue", value);

        String bootTimeValue = ConfigResolver.getPropertyValue("some.boottimekey");
        Assert.assertNotNull(bootTimeValue);
        Assert.assertEquals("correctvalue", bootTimeValue);
    }

}
