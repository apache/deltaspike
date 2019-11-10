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
import java.util.HashSet;

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
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import org.apache.deltaspike.test.core.api.config.injectable.numberconfig.NumberConfiguredBean;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


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

        assertEquals(14, settingsBean.getProperty1());
        assertEquals(7L, settingsBean.getProperty2());
        assertEquals(-7L, settingsBean.getInverseProperty2());

        // also check the ones with defaultValue
        assertEquals("14", settingsBean.getProperty3Filled());
        assertEquals("myDefaultValue", settingsBean.getProperty3Defaulted());
        assertEquals(42, settingsBean.getProperty4Defaulted());

        assertEquals("some setting for prodDB", settingsBean.getDbConfig());
    }

    @Test
    public void testBooleanPropertyInjection()
    {
        SettingsBean settingsBean = BeanProvider.getContextualReference(SettingsBean.class, false);
        assertEquals(Boolean.FALSE, settingsBean.getBooleanPropertyFalse());

        assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue1());
        assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue2());
        assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue3());
        assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue4());
        assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue5());
        assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue6());
        assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue7());
        assertEquals(Boolean.TRUE, settingsBean.getBooleanPropertyTrue8());
    }

    @Test
    public void testNmberConfigInjection()
    {
        NumberConfiguredBean numberBean = BeanProvider.getContextualReference(NumberConfiguredBean.class, false);
        assertNull(numberBean.getPropertyNonexisting());
        assertEquals(Float.valueOf(123.45f), numberBean.getPropertyFromConfig());
        assertEquals(Float.valueOf(42.42f), numberBean.getPropertyNonexistingDefaulted());
    }

    @Test
    public void testProjectStageAwareReplacement()
    {
        SettingsBean settingsBean = BeanProvider.getContextualReference(SettingsBean.class, false);
        assertEquals("https://myapp/login.xhtml", settingsBean.getProjectStageAwareVariableValue());
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


    @Test
    public void proxy() throws MalformedURLException
    {
        ConfigBean settingsBean = BeanProvider.getContextualReference(ConfigBean.class);

        assertEquals(14, settingsBean.intProperty1());
        assertEquals("14", settingsBean.stringProperty3Filled());
        assertEquals("myDefaultValue", settingsBean.stringProperty3Defaulted());
        assertEquals(42, settingsBean.intProperty4Defaulted().intValue());

        assertEquals("some setting for prodDB", settingsBean.dbConfig());
        assertEquals(Boolean.FALSE, settingsBean.booleanPropertyFalse());
        assertEquals(Boolean.TRUE, settingsBean.booleanPropertyTrue1());
        assertEquals(Boolean.TRUE, settingsBean.booleanPropertyTrue2());
        assertEquals(Boolean.TRUE, settingsBean.booleanPropertyTrue3());
        assertEquals(Boolean.TRUE, settingsBean.booleanPropertyTrue4());
        assertEquals(Boolean.TRUE, settingsBean.booleanPropertyTrue5());
        assertEquals(Boolean.TRUE, settingsBean.booleanPropertyTrue6());
        assertEquals(Boolean.TRUE, settingsBean.booleanPropertyTrue7());
        assertEquals(Boolean.TRUE, settingsBean.booleanPropertyTrue8());
        assertEquals(asList(new URL("http://localhost"), new URL("http://127.0.0.1")), settingsBean.urlList());
        assertEquals(asList("http://localhost", "http://127.0.0.1"), settingsBean.defaultListHandling());
        assertEquals(new HashSet<Integer>(asList(1, 2)), settingsBean.defaultSetHandling());
        assertEquals(singletonList(new URL("http://127.0.0.2")), settingsBean.urlListFromProperties());
        assertEquals("value", settingsBean.customSourceValue());
    }

    @Test
    public void proxyPrefix() throws MalformedURLException
    {
        PrefixedConfigBean settingsBean = BeanProvider.getContextualReference(PrefixedConfigBean.class);
        assertEquals("done", settingsBean.value());
    }
}
