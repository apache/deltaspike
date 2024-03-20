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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.0.0
 */
public class UrlConverterTest
{
    @Test
    public void testConvertingNormalUrl() throws MalformedURLException
    {
        UrlConverter converter = new UrlConverter();

        final URL expected = new URL("https://gitlab.com/");
        final URL actual = converter.convert("https://gitlab.com/");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testFilteredProtocolPass() throws MalformedURLException, URISyntaxException
    {
        List<String> protocols = new ArrayList<>();
        protocols.add("http");
        protocols.add("https");

        UrlConverter converter = new UrlConverter(protocols);

        final URI expected = new URL("https://gitlab.com/").toURI();
        final URI actual = converter.convert("https://gitlab.com/").toURI();

        Assert.assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadProtocol()
    {
        List<String> protocols = new ArrayList<>();
        protocols.add("https");

        UrlConverter converter = new UrlConverter(protocols);
        converter.convert("http://gitlab.com/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidProtocol()
    {
        UrlConverter converter = new UrlConverter();
        converter.convert(":invalid.protocol");
    }
}
