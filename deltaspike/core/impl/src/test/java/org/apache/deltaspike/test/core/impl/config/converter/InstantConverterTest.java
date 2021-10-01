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

import org.apache.deltaspike.core.impl.config.converter.InstantConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * @since 2.0.0
 */
public class InstantConverterTest
{
    private InstantConverter converter;

    @Before
    public void before()
    {
        converter = new InstantConverter();
    }

    @Test
    public void testConvertingInstantString()
    {
        final Instant expected = Instant.ofEpochMilli(1196676930000L);
        final Instant actual = converter.convert("2007-12-03T10:15:30.00Z");

        Assert.assertEquals(expected, actual);
    }

    @Test(expected = DateTimeParseException.class)
    public void testText()
    {
        converter.convert("Hello, world!");
    }

    @Test(expected = DateTimeParseException.class)
    public void testLocalizedNumber()
    {
        converter.convert("200,000,000,000");
    }
}
