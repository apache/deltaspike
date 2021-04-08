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
 * @since 1.9.5
 */
public class ColorConverterTest {

    private ColorConverter converter;

    @Before
    public void before() {
        converter = new ColorConverter();
    }

    @Test
    public void testConverteringPattern() {
        final Color expected = Color.BLACK;
        final Color actual = converter.convert("#000000");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConverteringPatternWithAlpha() {
        final Color expected = Color.LIGHT_GRAY;
        final Color actual = converter.convert("#C0C0C0FF");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConverteringPattern3Digit() {
        final Color expected = Color.WHITE;
        final Color actual = converter.convert("#FFF");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConverteringPattern4Digit() {
        final Color expected = Color.YELLOW;
        final Color actual = converter.convert("#FF0F");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConverteringLiteralHex() {
        final Color expected = Color.BLUE;
        final Color actual = converter.convert("0x0000FF");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConverteringColorName() {
        final Color expected = Color.WHITE;
        final Color actual = converter.convert("white");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConverteringColorNameCaps() {
        final Color expected = Color.LIGHT_GRAY;
        final Color actual = converter.convert("LIGHTGRAY");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testInvalidNumber3() {
        try
        {
            converter.convert("#FFZ");
            Assert.fail();
        }
        catch (NumberFormatException ex)
        {
            // Do nothing
        }
    }

    @Test
    public void testInvalidNumber4() {
        try
        {
            converter.convert("#FFFY");
            Assert.fail();
        }
        catch (NumberFormatException ex)
        {
            // Do nothing
        }
    }

    @Test
    public void testColorBlank() {
        try
        {
            converter.convert("#");
            Assert.fail();
        }
        catch (IllegalArgumentException ex)
        {
            // Do nothing
        }
    }

    @Test
    public void testColorInvalidLength() {
        try
        {
            converter.convert("#F");
            Assert.fail();
        }
        catch (IllegalArgumentException ex)
        {
            // Do nothing
        }
    }
}
