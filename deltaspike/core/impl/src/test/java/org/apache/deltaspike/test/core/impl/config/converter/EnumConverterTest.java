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

import org.apache.deltaspike.core.impl.config.converter.EnumConverter;
import org.junit.Assert;
import org.junit.Test;

import java.time.DayOfWeek;
import java.util.concurrent.TimeUnit;

/**
 * @since 2.0.0
 */
public class EnumConverterTest
{
    @Test
    public void testConvertTimeUnit()
    {
        EnumConverter converter = new EnumConverter();

        final TimeUnit expected = TimeUnit.NANOSECONDS;
        final Enum actual = converter.convert("java.util.concurrent.TimeUnit.NANOSECONDS");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertDayOfWeek()
    {
        EnumConverter converter = new EnumConverter();

        final DayOfWeek expected = DayOfWeek.MONDAY;
        final Enum actual = converter.convert("java.time.DayOfWeek#MONDAY");

        Assert.assertEquals(expected, actual);
    }

    /**
     * When the {@link EnumConverter} is constructed with a type,
     * it's permissible to only pass the {@link Enum} constant
     * name without the package or class name.
     */
    @Test
    public void testEnumNameOnlyFromTypedInstance()
    {
        EnumConverter converter = new EnumConverter(DayOfWeek.class);

        final DayOfWeek expected = DayOfWeek.MONDAY;
        final Enum actual = converter.convert("MONDAY");

        Assert.assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertWroteEnumType()
    {
        EnumConverter converter = new EnumConverter(TimeUnit.class);
        converter.convert("java.time.DayOfWeek#MONDAY");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBrokenNamingConvention()
    {
        EnumConverter converter = new EnumConverter();
        converter.convert("JAVA-TIME-DAYOFWEEK#MONDAY");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonEnumClasses()
    {
        EnumConverter converter = new EnumConverter();
        converter.convert("java.lang.String#MONDAY");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistingClasses()
    {
        EnumConverter converter = new EnumConverter();
        converter.convert("java.lang.does.not.exist#MONDAY");
    }
}
