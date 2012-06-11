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
package org.apache.deltaspike.test.core.api.config.injectable;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.apache.deltaspike.test.util.FileUtils;
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

import java.net.URL;

@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class InjectableConfigPropertyTest
{
    /**
     *X TODO creating a WebArchive is only a workaround because JavaArchive cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {
        URL fileUrl = InjectableConfigPropertyTest.class.getClassLoader()
                .getResource("META-INF/apache-deltaspike.properties");

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "injectableConfigPropertyTest.jar")
                .addPackage("org.apache.deltaspike.test.core.api.config.injectable")
                .addPackage("org.apache.deltaspike.test.category")
                .addAsManifestResource(FileUtils.getFileForURL(fileUrl.toString()))
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, "beanProvider.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void injectionViaConfigProperty()
    {
        SettingsBean settingsBean = BeanProvider.getContextualReference(SettingsBean.class, false);

        Assert.assertEquals(14, settingsBean.getProperty1());
        Assert.assertEquals(7L, settingsBean.getProperty2());
        Assert.assertEquals(-14, settingsBean.getInverseProperty1());
        Assert.assertEquals(-7L, settingsBean.getInverseProperty2());
    }
}
