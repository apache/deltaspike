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

import org.apache.deltaspike.core.impl.config.converter.UriConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @since 2.0.0
 */
public class UriConverterTest
{
    private UriConverter converter;

    @Before
    public void before() {
        converter = new UriConverter();
    }

    @Test
    public void testUrl() throws URISyntaxException
    {
        final URI expected = new URI("https://apache.org/");
        final URI actual = converter.convert("https://apache.org/");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testMailto() throws URISyntaxException
    {
        final URI expected = new URI("mailto:java-net@java.sun.com");
        final URI actual = converter.convert("mailto:java-net@java.sun.com");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testSteamUrl() throws URISyntaxException
    {
        final URI expected = new URI("steam://connect/192.0.2.1:27015");
        final URI actual = converter.convert("steam://connect/192.0.2.1:27015");

        Assert.assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmpty()
    {
        converter.convert("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhitespace()
    {
        converter.convert(" ");
    }
}
