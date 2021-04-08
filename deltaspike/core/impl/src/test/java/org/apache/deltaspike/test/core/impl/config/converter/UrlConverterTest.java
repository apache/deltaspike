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

import org.apache.deltaspike.core.impl.config.converter.UrlConverter;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.9.5
 */
public class UrlConverterTest
{

    @Test
    public void testConverteringNormalUrl() throws MalformedURLException
    {
        UrlConverter converter = new UrlConverter();

        final URL expected = new URL("https://elypia.org/");
        final URL actual = converter.convert("https://elypia.org/");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testFilteredProtocolPass() throws MalformedURLException
    {
        List<String> protocols = new ArrayList<>();
        protocols.add("http");
        protocols.add("https");

        UrlConverter converter = new UrlConverter(protocols);

        final URL expected = new URL("https://gitlab.com/");
        final URL actual = converter.convert("https://gitlab.com/");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testBadProtocol() {
        List<String> protocols = new ArrayList<>();
        protocols.add("https");

        UrlConverter converter = new UrlConverter(protocols);

        try
        {
            converter.convert("http://elypia.org/");
            Assert.fail();
        }
        catch (IllegalArgumentException ex)
        {
            // Do nothing
        }
    }

    @Test
    public void testInvalidProtocol() {
        UrlConverter converter = new UrlConverter();

        try
        {
            converter.convert(":invalid.protocol");
            Assert.fail();
        }
        catch (IllegalArgumentException ex)
        {
            // Do nothing
        }
    }
}
