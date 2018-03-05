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
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class TypedResolverTest
{
    @Before
    public void init()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.UnitTest);
    }

    @Test
    public void testValidTypes()
    {
        Assert.assertEquals("configured", ConfigResolver.resolve("deltaspike.test.string-value").getValue());

        Assert.assertEquals(Boolean.FALSE, ConfigResolver.resolve("deltaspike.test.boolean-value").as(Boolean.class)
                .getValue());

        Assert.assertEquals(TestConfigSource.class, ConfigResolver.resolve("deltaspike.test.class-value").as(Class
                .class).getValue());

        Assert.assertEquals(5l, (int) ConfigResolver.resolve("deltaspike.test.integer-value").as(Integer.class)
                .getValue());

        Assert.assertEquals(8589934592l, (long) ConfigResolver.resolve("deltaspike.test.long-value").as(Long.class)
                .getValue());

        Assert.assertEquals(-1.1f, (float) ConfigResolver.resolve("deltaspike.test.float-value").as(Float.class)
                .getValue(), 0);

        Assert.assertEquals(4e40d, (double) ConfigResolver.resolve("deltaspike.test.double-value").as(Double.class)
                .getValue(), 0);
    }

    @Test
    public void testConverter()
    {
        Assert.assertEquals(new GregorianCalendar(2014, 12, 24).getTime(),
                ConfigResolver.resolve("deltaspike.test.date-value")
                        .as(Date.class, new TestDateConverter()).getValue());

        // test fallback to default
        Assert.assertEquals(new GregorianCalendar(2015, 01, 01).getTime(),
                ConfigResolver.resolve("deltaspike.test.INVALID-date-value")
                        .as(Date.class, new TestDateConverter())
                        .withDefault(new GregorianCalendar(2015, 01, 01).getTime())
                        .getValue());
    }

    @Test
    public void testProjectStageAware()
    {
        Assert.assertEquals("unittestvalue",
                ConfigResolver.resolve("testkey")
                        .withCurrentProjectStage(true)
                        .getValue());

        Assert.assertEquals("testvalue",
                ConfigResolver.resolve("testkey")
                        .withCurrentProjectStage(false)
                        .getValue());

        // property without PS, with PS-aware
        Assert.assertEquals("testvalue",
                ConfigResolver.resolve("testkey2")
                        .withCurrentProjectStage(true)
                        .getValue());
    }

    @Test
    public void testParameterized()
    {
        // param OK, ps OK
        Assert.assertEquals("TestDataSource",
                ConfigResolver.resolve("dataSource")
                        .withCurrentProjectStage(true)
                        .parameterizedBy("dbvendor")
                        .getValue());

        // param OK, NO ps
        Assert.assertEquals("PostgreDataSource",
                ConfigResolver.resolve("dataSource")
                        .withCurrentProjectStage(false)
                        .parameterizedBy("dbvendor")
                        .getValue());

        // param doesn't resolve, ps OK
        Assert.assertEquals("UnitTestDataSource",
                ConfigResolver.resolve("dataSource")
                        .withCurrentProjectStage(true)
                        .parameterizedBy("INVALIDPARAMETER")
                        .getValue());

        // param OK, ps OK, NO base.param.ps, NO base.param, fall back to base.ps
        Assert.assertEquals("UnitTestDataSource",
                ConfigResolver.resolve("dataSource")
                        .withCurrentProjectStage(true)
                        .parameterizedBy("dbvendor3")
                        .getValue());

        // param OK, NO ps, base.param undefined, fall back to base
        Assert.assertEquals("DefaultDataSource",
                ConfigResolver.resolve("dataSource")
                        .withCurrentProjectStage(false)
                        .parameterizedBy("dbvendor3")
                        .getValue());
    }

    @Test
    public void testDefault()
    {
        Assert.assertEquals(10l,
                (long) ConfigResolver.resolve("INVALIDKEY")
                .as(Long.class)
                .withDefault(10l).getValue());

        // string default
        Assert.assertEquals(10l,
                (long) ConfigResolver.resolve("INVALIDKEY")
                        .as(Long.class)
                        .withStringDefault("10").getValue());
    }

    @Test
    public void testStrict()
    {
        Assert.assertEquals("TestDataSource",
                ConfigResolver.resolve("dataSource")
                        .withCurrentProjectStage(true)
                        .parameterizedBy("dbvendor")
                        .strictly(true)
                        .getValue());

        // no base.param, no value for base.param.ps
        Assert.assertEquals(null,
                ConfigResolver.resolve("dataSource")
                        .withCurrentProjectStage(true)
                        .parameterizedBy("dbvendor3")
                        .strictly(true)
                        .getValue());

        // valid base.param, but no base.param.ps
        Assert.assertEquals(null,
                ConfigResolver.resolve("dataSource")
                        .withCurrentProjectStage(true)
                        .parameterizedBy("dbvendor2")
                        .strictly(true)
                        .getValue());
    }

    @Test
    public void testGets()
    {
        ConfigResolver.TypedResolver<String> resolver = ConfigResolver.resolve("dataSource")
                .withCurrentProjectStage(true)
                .parameterizedBy("dbvendor")
                .withDefault("TESTDEFAULT");

        Assert.assertEquals("TestDataSource", resolver.getValue());
        Assert.assertEquals("dataSource", resolver.getKey());
        Assert.assertEquals("TESTDEFAULT", resolver.getDefaultValue());
        Assert.assertEquals("dataSource.mysql.UnitTest", resolver.getResolvedKey());


        ConfigResolver.TypedResolver<String> resolver2 = ConfigResolver.resolve("testkey2")
                .withCurrentProjectStage(true)
                .parameterizedBy("INVALIDPARAMETER");


        Assert.assertEquals("testvalue", resolver2.getValue());
        Assert.assertEquals("testkey2", resolver2.getResolvedKey());
    }

    @Test
    public void testWithCacheTime() throws Exception
    {
        ConfigResolver.TypedResolver<String> resolver = ConfigResolver.resolve("dataSource")
            .withCurrentProjectStage(true)
            .parameterizedBy("dbvendor")
            .cacheFor(TimeUnit.MILLISECONDS, 5)
            .withDefault("TESTDEFAULT");

        Assert.assertEquals("TestDataSource", resolver.getValue());
        Assert.assertEquals("TestDataSource", resolver.getValue());
        Assert.assertEquals("dataSource", resolver.getKey());
        Assert.assertEquals("TESTDEFAULT", resolver.getDefaultValue());
        Assert.assertEquals("dataSource.mysql.UnitTest", resolver.getResolvedKey());

        // because the clock steps in certain OS is only 16ms
        Thread.sleep(35L);
        Assert.assertEquals("TestDataSource", resolver.getValue());
    }

    public static class TestDateConverter implements ConfigResolver.Converter<Date> {

        @Override
        public Date convert(String value)
        {
            String[] parts = value.split("-");
            return new GregorianCalendar(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]),
                    Integer.valueOf(parts[2])).getTime();
        }
    }


}
