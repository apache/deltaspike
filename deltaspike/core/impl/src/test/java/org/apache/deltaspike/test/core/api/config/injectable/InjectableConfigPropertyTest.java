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

import java.net.MalformedURLException;
import java.net.URL;

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

import org.apache.deltaspike.test.core.api.config.injectable.numberconfig.NumberConfiguredBean;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;


@RunWith(Arquillian.class)
@Category(SeCategory.class) //X TODO this is only SeCategory as there is currently an Arq problem with properties!
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
                .addPackage(SettingsBean.class.getPackage())
                .addPackage(NumberConfiguredBean.class.getPackage())
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
        Assert.assertEquals(-7L, settingsBean.getInverseProperty2());

        // also check the ones with defaultValue
        Assert.assertEquals("14", settingsBean.getProperty3Filled());
        Assert.assertEquals("myDefaultValue", settingsBean.getProperty3Defaulted());
        Assert.assertEquals(42, settingsBean.getProperty4Defaulted());

        Assert.assertEquals("some setting for prodDB", settingsBean.getDbConfig());
    }

    @Test
    public void testBooleanPropertyInjection()
    {
        SettingsBean settingsBean = BeanProvider.getContextualReference(SettingsBean.class, false);
        Assert.assertEquals(Boolean.FALSE, settingsBean.getBooleanPropertyFalse());

        Assert.assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue1());
        Assert.assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue2());
        Assert.assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue3());
        Assert.assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue4());
        Assert.assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue5());
        Assert.assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue6());
        Assert.assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue7());
        Assert.assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue8());
    }

    @Test
    public void testNmberConfigInjection()
    {
        NumberConfiguredBean numberBean = BeanProvider.getContextualReference(NumberConfiguredBean.class, false);
        Assert.assertNull(numberBean.getPropertyNonexisting());
        Assert.assertEquals(Float.valueOf(123.45f), numberBean.getPropertyFromConfig());
        Assert.assertEquals(Float.valueOf(42.42f), numberBean.getPropertyNonexistingDefaulted());
    }

    @Test
    public void checkDynamicConvertedInjections() throws MalformedURLException
    {
        SettingsBean settingsBean = BeanProvider.getContextualReference(SettingsBean.class, false);
        assertEquals(asList(new URL("http://localhost"), new URL("http://127.0.0.1")), settingsBean.getUrlList());
        assertEquals(singletonList(new URL("http://127.0.0.2")), settingsBean.getUrlListFromProperties());
    }

    @Test
    public void checkCdiSourceFilter() throws MalformedURLException
    {
        SettingsBean settingsBean = BeanProvider.getContextualReference(SettingsBean.class, false);
        assertEquals("value", settingsBean.getCustomSourceValue());
    }
}
