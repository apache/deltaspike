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
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link org.apache.deltaspike.core.spi.config.ConfigSource}
 */
public class ConfigSourceTest
{
    @Test
    public void testConfigViaSystemProperty()
    {
        String key = "testProperty01";
        String value = "test_value_01";
        System.setProperty(key, value);
        
        String configuredValue = ConfigResolver.getPropertyValue(key);
        
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
    public void testConfigViaMetaInfPropertyFile()
    {
        String key = "testProperty03";
        String value = "test_value_03";

        String configuredValue = ConfigResolver.getPropertyValue(key);

        Assert.assertEquals(value, configuredValue);
    }

    /*
    //X TODO discuss marker
    @Test
    public void testConfigViaSystemPropertyAndMarker()
    {
        String key = "testProperty01";
        String value = "test_value";
        System.setProperty("org.apache.deltaspike." + key, value);

        String configuredValue = ConfigResolver.getPropertyValue("@@deltaspike@@" + key);

        Assert.assertEquals(value, configuredValue);
    }
    */
}
