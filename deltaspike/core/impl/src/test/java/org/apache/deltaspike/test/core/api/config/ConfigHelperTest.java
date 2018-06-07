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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigHelperTest {

    @Test
    public void testDiffConfig() {
        ConfigResolver.ConfigHelper cfgHelper = ConfigResolver.getConfigProvider().getHelper();

        Map<String, String> oldVal = new HashMap<>();
        Map<String, String> newVal = new HashMap<>();

        oldVal.put("a", "1");

        newVal.put("b", "2");
        newVal.put("a", "1");

        assertAll(cfgHelper.diffConfig(null, newVal), "a", "b");
        assertAll(cfgHelper.diffConfig(oldVal, null), "a");
        assertAll(cfgHelper.diffConfig(oldVal, newVal), "b");
        assertAll(cfgHelper.diffConfig(oldVal, oldVal));
        assertAll(cfgHelper.diffConfig(newVal, newVal));

        newVal.put("a", "5");
        assertAll(cfgHelper.diffConfig(oldVal, newVal), "a", "b");

    }

    private void assertAll(Set<String> actualVals, String... expectedVals) {
        Assert.assertNotNull(actualVals);
        Assert.assertEquals(expectedVals.length, actualVals.size());

        for (String expectedVal : expectedVals) {
            Assert.assertTrue(actualVals.contains(expectedVal));
        }
    }

}
