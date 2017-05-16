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
package org.apache.deltaspike.test.core.api.config;

import java.io.File;
import java.io.FileWriter;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link org.apache.deltaspike.core.spi.config.ConfigSource}
 */
public class ConfigSourceTest
{
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testConfigViaSystemProperty()
    {
        String key = "testProperty01";
        String value = "test_value_01";

        // the value is not yet configured
        String configuredValue = ConfigResolver.getPropertyValue(key);
        Assert.assertNull(configuredValue);

        String myDefaultValue = "theDefaultValueDummy";
        configuredValue = ConfigResolver.getPropertyValue(key, myDefaultValue);
        Assert.assertEquals(myDefaultValue, configuredValue);

        // now we set a value for the config key
        System.setProperty(key, value);
        configuredValue = ConfigResolver.getPropertyValue(key);
        Assert.assertEquals(value, configuredValue);

        System.setProperty(key, "");
        configuredValue = ConfigResolver.getPropertyValue(key);
        Assert.assertEquals("", configuredValue);

    }

    @Test
    public void testConfigViaClasspathPropertyFile()
    {
        String key = "testProperty02";
        String value = "test_value_02";

        String configuredValue = ConfigResolver.getPropertyValue(key);
        Assert.assertEquals(value, configuredValue);
    }

    @Test
    public void testConfigUninitializedDefaultValue()
    {
        String key = "nonexistingProperty01";

        // passing in null as default value should work fine
        String configuredValue = ConfigResolver.getPropertyValue(key, null);
        Assert.assertNull(configuredValue);

        String myDefaultValue = "theDefaultValueDummy";
        configuredValue = ConfigResolver.getPropertyValue(key, myDefaultValue);
        Assert.assertEquals(myDefaultValue, configuredValue);
    }

    @Test
    public void testConfigViaMetaInfPropertyFile()
    {
        String key = "testProperty03";
        String value = "test_value_03";

        String configuredValue = ConfigResolver.getPropertyValue(key);
        Assert.assertEquals(value, configuredValue);
    }

    @Test
    public void testConfigFilter()
    {
        String secretVal = ConfigResolver.getPropertyValue("my.very.secret");
        Assert.assertNotNull(secretVal);
        Assert.assertEquals("a secret value: onlyIDoKnowIt", secretVal);
    }

    @Test
    public void testEnvProperties() {
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome == null || javaHome.isEmpty())
        {
            // weird, should exist. Anyway, in that case we cannot test it.
            return;
        }

        // we search for JAVA.HOME which should also give us JAVA_HOME
        String value = ConfigResolver.getPropertyValue("JAVA.HOME");
        Assert.assertNotNull(value);
        Assert.assertEquals(javaHome, value);
    }


    @Test
    public void testUserHomeConfigProperties() throws Exception {
        String userHomeKey = "user.home";
        String oldUserHome = System.getProperty(userHomeKey);
        try
        {
            File newUserHomeFolder = temporaryFolder.newFolder();
            System.setProperty(userHomeKey, newUserHomeFolder.getAbsolutePath());

            File dsHomeConfig = new File(newUserHomeFolder, ".deltaspike/apache-deltaspike.properties");
            dsHomeConfig.getParentFile().mkdirs();

            FileWriter fw = new FileWriter(dsHomeConfig);
            fw.write("ds.test.fromHome=withLove\ndeltaspike_ordinal=123");

            fw.close();

            // force freshly picking up all ConfigSources for this test
            ConfigResolver.freeConfigSources();

            Assert.assertEquals("withLove", ConfigResolver.getPropertyValue("ds.test.fromHome"));
        }
        finally
        {
            System.setProperty(userHomeKey, oldUserHome);
            ConfigResolver.freeConfigSources();
        }
    }


}
