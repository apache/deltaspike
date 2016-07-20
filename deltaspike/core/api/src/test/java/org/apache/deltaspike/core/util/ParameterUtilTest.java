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

package org.apache.deltaspike.core.util;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

public class ParameterUtilTest
{
    @Before
    public void isEnabled()
    {
        Assume.assumeTrue(ParameterUtil.isParameterSupported());
    }

    @Test
    public void shouldReturnNameOrNull() throws Exception
    {
        Method method = getClass().getDeclaredMethod("someMethod", String.class);
        String parameterName = ParameterUtil.getName(method, 0);
        Assert.assertTrue(parameterName.equals("arg0") || parameterName.equals("firstParameter"));
    }

    public void someMethod(String firstParameter) {}
}