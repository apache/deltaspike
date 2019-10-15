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
package org.apache.deltaspike.test.api.util;

import org.apache.deltaspike.core.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest
{
    @Test
    public void emptyStringDetection()
    {
        Assert.assertTrue(StringUtils.isEmpty(null));
        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertTrue(StringUtils.isEmpty(" "));
        Assert.assertFalse(StringUtils.isEmpty(" a "));
    }

    @Test
    public void testRemoveSpecialChars() {
        Assert.assertNull(StringUtils.removeSpecialChars(null));
        Assert.assertEquals("abc_def", StringUtils.removeSpecialChars("abc def"));
        Assert.assertEquals("a_c_def", StringUtils.removeSpecialChars("a_c def")); // not replace _
        Assert.assertEquals("a-c_dex", StringUtils.removeSpecialChars("a-c dex")); // not replace -
        Assert.assertEquals("a_c_def", StringUtils.removeSpecialChars("a\'c def"));
        Assert.assertEquals("A_c_deX", StringUtils.removeSpecialChars("A#c deX"));
    }
}