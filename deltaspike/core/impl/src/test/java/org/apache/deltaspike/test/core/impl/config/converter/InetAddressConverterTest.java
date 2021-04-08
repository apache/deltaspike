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
package org.apache.deltaspike.test.core.impl.config.converter;

import org.apache.deltaspike.core.impl.config.converter.InetAddressConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @since 1.9.5
 */
public class InetAddressConverterTest
{
    private InetAddressConverter converter;

    @Before
    public void before()
    {
        converter = new InetAddressConverter();
    }

    @Test
    public void testConverteringIpv4() throws UnknownHostException
    {
        final InetAddress expected = InetAddress.getByName("192.168.0.1");
        final InetAddress actual = converter.convert("192.168.0.1");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConverteringIpv6() throws UnknownHostException
    {
        final InetAddress expected = InetAddress.getByName("2001:db8:0:1234:0:567:8:1");
        final InetAddress actual = converter.convert("2001:db8:0:1234:0:567:8:1");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConverteringLocalhost() throws UnknownHostException
    {
        final InetAddress expected = InetAddress.getByName("127.0.0.1");
        final InetAddress actual = converter.convert("localhost");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testText() {
        try
        {
            converter.convert("Hello, world!");
            Assert.fail();
        }
        catch (IllegalArgumentException ex)
        {
            // Do nothing
        }
    }

    @Test
    public void testInvalidIp() {
        try
        {
            converter.convert("512.512.512.512");
            Assert.fail();
        }
        catch (IllegalArgumentException ex)
        {
            // Do nothing
        }
    }
}
