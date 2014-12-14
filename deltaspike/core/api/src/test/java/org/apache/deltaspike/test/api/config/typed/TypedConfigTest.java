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
package org.apache.deltaspike.test.api.config.typed;

import org.apache.deltaspike.core.api.config.base.CoreBaseConfig;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.test.api.config.TestConfigSource;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class TypedConfigTest
{
    @Test
    public void testConfiguredDefaultValue()
    {
        Integer configuredValue = CoreBaseConfig.Interceptor.PRIORITY.getValue();
        Assert.assertEquals(new Integer(0), configuredValue);

        Integer defaultValue = CoreBaseConfig.Interceptor.PRIORITY.getDefaultValue();
        Assert.assertEquals(new Integer(0), defaultValue);
    }

    @Test
    public void testStringValue()
    {
        String configuredValue = TestConfig.Valid.STRING_VALUE.getValue();
        Assert.assertEquals("configured", configuredValue);

        String defaultValue = TestConfig.Valid.STRING_VALUE.getDefaultValue();
        Assert.assertEquals("default", defaultValue);
    }

    @Test
    public void testIntegerValue()
    {
        Integer configuredValue = TestConfig.Valid.INTEGER_VALUE.getValue();
        Assert.assertEquals(new Integer(5), configuredValue);

        Integer defaultValue = TestConfig.Valid.INTEGER_VALUE.getDefaultValue();
        Assert.assertEquals(new Integer(14), defaultValue);
    }

    @Test
    public void testFloatValue()
    {
        Float configuredValue = TestConfig.Valid.FLOAT_VALUE.getValue();
        Assert.assertEquals(new Float(-1.1), configuredValue);

        Float defaultValue = TestConfig.Valid.FLOAT_VALUE.getDefaultValue();
        Assert.assertEquals(new Float(1.1), defaultValue);
    }

    @Test
    public void testBooleanValue()
    {
        Boolean configuredValue = TestConfig.Valid.BOOLEAN_VALUE.getValue();
        Assert.assertEquals(Boolean.FALSE, configuredValue);

        Boolean defaultValue = TestConfig.Valid.BOOLEAN_VALUE.getDefaultValue();
        Assert.assertEquals(Boolean.TRUE, defaultValue);
    }

    @Test
    public void testClassValue()
    {
        Class configuredValue = TestConfig.Valid.CLASS_VALUE.getValue();
        Assert.assertEquals(TestConfigSource.class, configuredValue);

        Class defaultValue = TestConfig.Valid.CLASS_VALUE.getDefaultValue();
        Assert.assertEquals(getClass(), defaultValue);
    }

    @Test
    public void testCustomType()
    {
        Date configuredValue = TestConfig.Valid.CUSTOM_TYPE_VALUE.getValue();
        Assert.assertEquals(configuredValue.getYear(), new Date().getYear());

        Date defaultValue = TestConfig.Valid.CUSTOM_TYPE_VALUE.getDefaultValue();
        Assert.assertEquals(new Date(1983, 4, 14), defaultValue);
    }

    @Test
    public void testIntegerValueNoValue()
    {
        Integer configuredValue = TestConfig.Valid.INTEGER_VALUE_NO_DEFAULT.getValue();
        Assert.assertEquals(null, configuredValue);

        Integer defaultValue = TestConfig.Valid.INTEGER_VALUE_NO_DEFAULT.getDefaultValue();
        Assert.assertEquals(null, defaultValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidConfigEntry()
    {
        try
        {
            TestConfig.InvalidConfig.CONFIG_WITHOUT_TYPE_INFORMATION.getValue();
        }
        catch (ExceptionInInitializerError e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e.getCause());
        }
        Assert.fail();
    }

    @Test(expected = NumberFormatException.class)
    public void testWrongConfigValue()
    {
        TestConfig.InvalidConfigValue.WRONG_VALUE.getValue();
    }
}
