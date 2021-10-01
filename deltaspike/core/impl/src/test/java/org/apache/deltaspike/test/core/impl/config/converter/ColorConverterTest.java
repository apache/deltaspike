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

import org.apache.deltaspike.core.impl.config.converter.ColorConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

/**
 * @since 2.0.0
 */
public class ColorConverterTest
{
    private ColorConverter converter;

    @Before
    public void before()
    {
        converter = new ColorConverter();
    }

    @Test
    public void testConvertingPattern()
    {
        final Color expected = Color.BLACK;
        final Color actual = converter.convert("#000000");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertingPatternWithAlpha()
    {
        final Color expected = Color.LIGHT_GRAY;
        final Color actual = converter.convert("#C0C0C0FF");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertingPattern3Digit()
    {
        final Color expected = Color.WHITE;
        final Color actual = converter.convert("#FFF");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertingPattern4Digit()
    {
        final Color expected = Color.YELLOW;
        final Color actual = converter.convert("#FF0F");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertingLiteralHex()
    {
        final Color expected = Color.BLUE;
        final Color actual = converter.convert("0x0000FF");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertingColorName()
    {
        final Color expected = Color.WHITE;
        final Color actual = converter.convert("white");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertingColorNameCaps()
    {
        final Color expected = Color.LIGHT_GRAY;
        final Color actual = converter.convert("LIGHTGRAY");

        Assert.assertEquals(expected, actual);
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidNumber3()
    {
        converter.convert("#FFZ");
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidNumber4()
    {
        converter.convert("#FFFY");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testColorBlank()
    {
        converter.convert("#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testColorInvalidLength()
    {
        converter.convert("#F");
    }
}
