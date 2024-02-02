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

import org.apache.deltaspike.core.impl.config.converter.DimensionConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

/**
 * @since 2.0.0
 */
public class DimensionConverterTest
{
    private DimensionConverter converter;

    @Before
    public void before()
    {
        converter = new DimensionConverter();
    }

    @Test
    public void testConvertingDimension()
    {
        final Dimension expected = new Dimension(1920, 1080);
        final Dimension actual = converter.convert("1920x1080");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertingSquare()
    {
        final Dimension expected = new Dimension(512, 512);
        final Dimension actual = converter.convert("512");

        Assert.assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDimensions()
    {
        converter.convert("512n512");
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidNumberFormatException()
    {
        converter.convert("3000000000x100");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeDimension()
    {
        converter.convert("-512x512");
    }
}
