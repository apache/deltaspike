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

import org.apache.deltaspike.core.impl.config.converter.DurationConverter;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @since 2.0.0
 */
public class DurationConverterTest
{
    @Test
    public void testConvertingDurationString()
    {
        DurationConverter converter = new DurationConverter();

        final Duration expected = Duration.ofMinutes(3064);
        final Duration actual = converter.convert("P2DT3H4M");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertingCustomTemporal()
    {
        DurationConverter converter = new DurationConverter(ChronoUnit.SECONDS);

        final Duration expected = Duration.ofSeconds(4);
        final Duration actual = converter.convert("4");

        Assert.assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertingLocalizedNumber()
    {
        DurationConverter converter = new DurationConverter();
        converter.convert("1,000");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertingWords()
    {
        DurationConverter converter = new DurationConverter();
        converter.convert("Hello, world!");
    }
}
