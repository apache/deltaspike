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
package org.apache.deltaspike.test.api.config;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ConfigResolverTest
{
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
}
