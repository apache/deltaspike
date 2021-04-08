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
 * @since 1.9.5
 */
public class DurationConverterTest
{
    @Test
    public void testConverteringDefault()
    {
        DurationConverter converter = new DurationConverter();

        final Duration expected = Duration.ofSeconds(1);
        final Duration actual = converter.convert("1000");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConverteringDurationString()
    {
        DurationConverter converter = new DurationConverter();

        final Duration expected = Duration.ofMinutes(3064);
        final Duration actual = converter.convert("P2DT3H4M");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConverteringCustomTemporal()
    {
        DurationConverter converter = new DurationConverter(ChronoUnit.SECONDS);

        final Duration expected = Duration.ofSeconds(4);
        final Duration actual = converter.convert("4");

        Assert.assertEquals(expected, actual);
    }

    /**
     * As this is meant to be for technical usage, such as developers, or administrators
     * of software, we don't allow localized {@link Number}s such as <code>1,000</code>. Only programatically
     * correct values like <code>1000</code>.
     */
    @Test
    public void testConverteringInvalidNumbers() {
        DurationConverter converter = new DurationConverter();

        try
        {
            converter.convert("1,000");
            Assert.fail();
        }
        catch (IllegalArgumentException ex)
        {
            // Do nothing
        }
    }

    @Test
    public void testConverteringWords() {
        DurationConverter converter = new DurationConverter();

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
}
