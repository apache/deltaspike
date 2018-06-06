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

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.spi.config.ConfigFilter;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("Duplicates")
public class ConfigResolverTest
{
    private static final String DEFAULT_VALUE = "defaultValue";

    @Before
    public void init()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.UnitTest);
    }

    @Test
    public void testOverruledValue()
    {
        String result = ConfigResolver.getPropertyValue("test");

        Assert.assertEquals("test2", result);
    }

    @Test
    public void testOrderOfAllValues()
    {
        List<String> result = ConfigResolver.getAllPropertyValues("test");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("test1", result.get(0));
        Assert.assertEquals("test2", result.get(1));
    }

    @Test
    public void testStandaloneConfigSource()
    {
        Assert.assertNull(ConfigResolver.getPropertyValue("notexisting"));
        Assert.assertEquals("testvalue", ConfigResolver.getPropertyValue("testkey"));
    }

    @Test
    public void testGetProjectStageAwarePropertyValue()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.UnitTest);
        Assert.assertNull(ConfigResolver.getProjectStageAwarePropertyValue("notexisting", null));

        Assert.assertEquals("testvalue", ConfigResolver.getPropertyValue("testkey"));
        Assert.assertEquals("unittestvalue", ConfigResolver.getProjectStageAwarePropertyValue("testkey"));
        Assert.assertEquals("unittestvalue", ConfigResolver.getProjectStageAwarePropertyValue("testkey", null));

        Assert.assertEquals("testvalue", ConfigResolver.getPropertyValue("testkey2"));
        Assert.assertEquals("testvalue", ConfigResolver.getProjectStageAwarePropertyValue("testkey2"));
        Assert.assertEquals("testvalue", ConfigResolver.getProjectStageAwarePropertyValue("testkey2", null));

        Assert.assertEquals("testvalue", ConfigResolver.getPropertyValue("testkey3"));
        Assert.assertEquals("", ConfigResolver.getProjectStageAwarePropertyValue("testkey3"));
        Assert.assertEquals(DEFAULT_VALUE, ConfigResolver.getProjectStageAwarePropertyValue("testkey3", DEFAULT_VALUE));

        Assert.assertEquals(DEFAULT_VALUE, ConfigResolver.getProjectStageAwarePropertyValue("deltaspike.test.projectstagefallback", DEFAULT_VALUE));
        Assert.assertEquals("", ConfigResolver.getProjectStageAwarePropertyValue("deltaspike.test.projectstagefallback"));

        Assert.assertEquals(DEFAULT_VALUE, ConfigResolver.resolve("deltaspike.test.projectstagefallback").as(String.class).withDefault(DEFAULT_VALUE).withCurrentProjectStage(true).getValue());
        Assert.assertEquals("", ConfigResolver.resolve("deltaspike.test.projectstagefallback").as(String.class).withCurrentProjectStage(true).getValue());
    }

    @Test
    public void testGetPropertyAwarePropertyValue()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.UnitTest);

        Assert.assertNull(ConfigResolver.getPropertyAwarePropertyValue("notexisting", null));

        Assert.assertEquals("testvalue", ConfigResolver.getPropertyValue("testkey"));
        Assert.assertEquals("unittestvalue", ConfigResolver.getPropertyAwarePropertyValue("testkey", "dbvendor"));
        Assert.assertEquals("unittestvalue", ConfigResolver.getPropertyAwarePropertyValue("testkey", "dbvendor", null));

        Assert.assertEquals("testvalue", ConfigResolver.getPropertyValue("testkey2"));
        Assert.assertEquals("testvalue", ConfigResolver.getPropertyAwarePropertyValue("testkey2", "dbvendor"));
        Assert.assertEquals("testvalue", ConfigResolver.getPropertyAwarePropertyValue("testkey2", "dbvendor", null));

        Assert.assertEquals("testvalue", ConfigResolver.getPropertyValue("testkey3"));
        Assert.assertEquals("", ConfigResolver.getPropertyAwarePropertyValue("testkey3", "dbvendor"));
        Assert.assertEquals(DEFAULT_VALUE, ConfigResolver.getPropertyAwarePropertyValue("testkey3", "dbvendor", DEFAULT_VALUE));

        Assert.assertEquals("TestDataSource", ConfigResolver.getPropertyAwarePropertyValue("dataSource", "dbvendor"));
        Assert.assertEquals("PostgreDataSource", ConfigResolver.getPropertyAwarePropertyValue("dataSource", "dbvendor2"));
        Assert.assertEquals("UnitTestDataSource", ConfigResolver.getPropertyAwarePropertyValue("dataSource", "dbvendorX"));

        Assert.assertEquals("TestDataSource", ConfigResolver.getPropertyAwarePropertyValue("dataSource", "dbvendor", null));
        Assert.assertEquals("PostgreDataSource", ConfigResolver.getPropertyAwarePropertyValue("dataSource", "dbvendor2", null));
        Assert.assertEquals("UnitTestDataSource", ConfigResolver.getPropertyAwarePropertyValue("dataSource", "dbvendorX", null));
        Assert.assertEquals(DEFAULT_VALUE, ConfigResolver.getPropertyAwarePropertyValue("dataSourceX", "dbvendorX", DEFAULT_VALUE));
    }

    @Test
    public void testConfigFilter()
    {
        ConfigFilter configFilter = new TestConfigFilter();

        Assert.assertEquals("shouldGetDecrypted: value", configFilter.filterValue("somekey.encrypted", "value"));
        Assert.assertEquals("**********", configFilter.filterValueForLog("somekey.password", "value"));

        ConfigResolver.addConfigFilter(configFilter);

        Assert.assertEquals("shouldGetDecrypted: value", ConfigResolver.getPropertyValue("testkey4.encrypted"));
        Assert.assertEquals("shouldGetDecrypted: value", ConfigResolver.getProjectStageAwarePropertyValue("testkey4.encrypted"));
        Assert.assertEquals("shouldGetDecrypted: value", ConfigResolver.getProjectStageAwarePropertyValue("testkey4.encrypted", null));
        Assert.assertEquals("shouldGetDecrypted: value", ConfigResolver.getPropertyAwarePropertyValue("testkey4.encrypted", "dbvendor"));
        Assert.assertEquals("shouldGetDecrypted: value", ConfigResolver.getPropertyAwarePropertyValue("testkey4.encrypted", "dbvendor", null));

        List<String> allPropertyValues = ConfigResolver.getAllPropertyValues("testkey4.encrypted");
        Assert.assertNotNull(allPropertyValues);
        Assert.assertEquals(1, allPropertyValues.size());
        Assert.assertEquals("shouldGetDecrypted: value", allPropertyValues.get(0));

    }

    @Test
    public void testGetConfigFilter()
    {
        // make sure no ConfigFilter is left over from previous tests
        ConfigResolver.getConfigProvider().releaseConfig(ClassUtils.getClassLoader(null));

        List<ConfigFilter> configFilters = ConfigResolver.getConfigFilters();
        Assert.assertNotNull(configFilters);
        Assert.assertEquals(1, configFilters.size());
        Assert.assertEquals(SecretTestConfigFilter.class, configFilters.get(0).getClass());
    }

    @Test
    public void testConfigVariableReplacement()
    {
        {
            String url = ConfigResolver.getPropertyValue("deltaspike.test.someapp.soap.endpoint", "", true);
            Assert.assertEquals("http://localhost:12345/someservice/myendpoint", url);
        }

        {
            String url = ConfigResolver.getPropertyValue("deltaspike.test.someapp.soap.endpoint", true);
            Assert.assertEquals("http://localhost:12345/someservice/myendpoint", url);
        }
    }

    @Test
    public void testConfigVariableReplacementInDefault()
    {
        {
            String url = ConfigResolver.getPropertyValue("deltaspike.test.notexisting",
                    "url: ${deltaspike.test.host.url}", true);
            Assert.assertEquals("url: http://localhost:12345", url);
        }

        {
            String url = ConfigResolver.getPropertyValue("deltaspike.test.someapp.soap.endpoint", true);
            Assert.assertEquals("http://localhost:12345/someservice/myendpoint", url);
        }
    }

    @Test
    public void testConfigVariableNotExisting()
    {
        {
            String url = ConfigResolver.getPropertyValue("deltaspike.test.nonexisting.variable", "", true);
            Assert.assertEquals("${does.not.exist}/someservice/myendpoint", url);
        }
        {
            String url = ConfigResolver.getPropertyValue("deltaspike.test.nonexisting.variable", true);
            Assert.assertEquals("${does.not.exist}/someservice/myendpoint", url);
        }
    }

    @Test
    public void testConfigVariableRecursiveDeclaration()
    {
        String url = ConfigResolver.getPropertyValue("deltaspike.test.recursive.variable1", "", true);
        Assert.assertEquals("pre-crazy-post/ohgosh/crazy", url);

        ConfigResolver.TypedResolver<String> tr = ConfigResolver.resolve("deltaspike.test.recursive.variable1")
            .evaluateVariables(true).logChanges(true);
        Assert.assertEquals("pre-crazy-post/ohgosh/crazy", tr.getValue());
    }

    @Test
    public void testTypedResolver_NonExistingValue()
    {
        final String key = "non.existing.key";

        ConfigResolver.TypedResolver<String> resolver = ConfigResolver.resolve(key)
            .logChanges(true);

        Assert.assertNull(resolver.getValue());

        setTestConfigSourceValue(key, "somevalue");
        Assert.assertEquals("somevalue", resolver.getValue());

        setTestConfigSourceValue(key, null);
        Assert.assertNull(resolver.getValue());
    }
    
    @Test
    public void testTypedResolver_OnChange()
    {
        final String key = "non.existing.key";

        final AtomicInteger valueChanged = new AtomicInteger(0);

        ConfigResolver.TypedResolver<String> resolver = ConfigResolver.resolve(key)
            .logChanges(true)
            .onChange((k, oldValue, newValue) ->
            {
                Assert.assertEquals(key, k);
                valueChanged.incrementAndGet();
            });

        Assert.assertNull(resolver.getValue());

        setTestConfigSourceValue(key, "somevalue");
        Assert.assertEquals("somevalue", resolver.getValue());
        Assert.assertEquals(1, valueChanged.get());

        setTestConfigSourceValue(key, "newvalue");
        Assert.assertEquals("newvalue", resolver.getValue());
        Assert.assertEquals(2, valueChanged.get());

        // this time we do not change anything
        Assert.assertEquals("newvalue", resolver.getValue());
        Assert.assertEquals(2, valueChanged.get());

        // last change
        setTestConfigSourceValue(key, null);
        Assert.assertNull(resolver.getValue());
        Assert.assertEquals(3, valueChanged.get());
    }

    @Test
    public void testProjectStageAwarePropertyValueReference_1() {
        final String expectedFooUrl =
                "http://bar-dev/services";

        final String actualFooUrl =
                ConfigResolver.getProjectStageAwarePropertyValue(
                "foo.url");

        Assert.assertEquals(expectedFooUrl, actualFooUrl);
    }

    @Test
    public void testProjectStageAwarePropertyValueReference_2() {
        final String expected =
                "projectStageAware-exampleEntry-1-is-tomato-UnitTest";

        final String projectStageAwareExampleEntry1 =
                ConfigResolver.getProjectStageAwarePropertyValue(
                "deltaspike.test.exampleEntry-2", 
                "");

        Assert.assertEquals(expected, projectStageAwareExampleEntry1);
    }

    @Test
    public void testConfiguredListValues_WithWhitespace() {
        List<String> emails = ConfigResolver.resolve("test.list.value.emails").asList().getValue();
        Assert.assertNotNull(emails);
        Assert.assertEquals(3, emails.size());
        Assert.assertTrue(emails.contains("test1@apache.org"));
        Assert.assertTrue(emails.contains("test2@apache.org"));
        Assert.assertTrue(emails.contains("test3@apache.org"));
    }

    @Test
    public void testConfiguredListValues_WithEscaping() {
        List<String> escapedValues = ConfigResolver.resolve("test.list.value.escaped.list").asList().getValue();
        Assert.assertNotNull(escapedValues);
        Assert.assertEquals(3, escapedValues.size());
        Assert.assertTrue(escapedValues.contains("val,ue1"));
        Assert.assertTrue(escapedValues.contains("value2"));
        Assert.assertTrue(escapedValues.contains("val\\ue3"));
    }

    @Test
    public void testConfiguredListValues_OtherType() {
        List<Integer> intValues = ConfigResolver.resolve("test.list.intvalues").as(Integer.class).asList().getValue();
        Assert.assertNotNull(intValues);
        Assert.assertEquals(4, intValues.size());
        Assert.assertTrue(intValues.contains(3));
        Assert.assertTrue(intValues.contains(7));
        Assert.assertTrue(intValues.contains(11));
        Assert.assertTrue(intValues.contains(17));
    }

    @Test
    public void testConfiguredListValues_NotExisting() {
        List<Integer> intValues = ConfigResolver.resolve("test.list.not_existing").as(Integer.class).asList().getValue();
        Assert.assertNotNull(intValues);
        Assert.assertEquals(0, intValues.size());
    }

    @Test
    public void testConfiguredListValues_WithDefault() {
        List<Integer> intValues = ConfigResolver.resolve("test.list.not_existing").as(Integer.class).asList().withDefault(Arrays.asList(99, 88, 77)).getValue();
        Assert.assertNotNull(intValues);
        Assert.assertEquals(3, intValues.size());
        Assert.assertTrue(intValues.contains(99));
        Assert.assertTrue(intValues.contains(88));
        Assert.assertTrue(intValues.contains(77));
    }

    private void setTestConfigSourceValue(String key, String value)
    {
        ConfigSource[] configSources = ConfigResolver.getConfigSources();
        for (ConfigSource configSource : configSources)
        {
            if (configSource instanceof TestConfigSource)
            {
                if (value == null)
                {
                    configSource.getProperties().remove(key);
                }
                else
                {
                    configSource.getProperties().put(key, value);
                }

                break;
            }
        }
    }

    public static class TestConfigFilter implements ConfigFilter
    {
        @Override
        public String filterValue(String key, String value)
        {
            if (key.contains("encrypted"))
            {
                return "shouldGetDecrypted: " + value;
            }
            return value;
        }

        @Override
        public String filterValueForLog(String key, String value)
        {
            if (key.contains("password"))
            {
                return "**********";
            }
            return value;
        }
    }

}
