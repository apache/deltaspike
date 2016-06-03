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

public class OptionalUtilTest
{
    @Before
    public void isEnabled()
    {
        Assume.assumeTrue(OptionalUtil.isOptionalSupported());
    }

    @Test
    public void shouldIdentifyOptionalReturnType() throws Exception
    {
        Method empty = getOptionalClass().getMethod("empty");
        Assert.assertTrue(OptionalUtil.isOptionalReturned(empty));
    }

    @Test
    public void shouldReturnEmptyWhenGivenNull() throws Exception
    {
        Object wrapped = OptionalUtil.wrap(null);
        Method isPresent = getOptionalClass().getMethod("isPresent");
        Object invoke = isPresent.invoke(wrapped);
        Assert.assertEquals(invoke, Boolean.FALSE);
    }

    @Test
    public void shouldReturnNotEmptyWhenGivenNonnull() throws Exception
    {
        Object wrapped = OptionalUtil.wrap("String");
        Method isPresent = getOptionalClass().getMethod("isPresent");
        Object invoke = isPresent.invoke(wrapped);
        Assert.assertEquals(invoke, Boolean.TRUE);
    }

    private static Class<?> getOptionalClass() throws ClassNotFoundException {
        return Class.forName("java.util.Optional");
    }
}