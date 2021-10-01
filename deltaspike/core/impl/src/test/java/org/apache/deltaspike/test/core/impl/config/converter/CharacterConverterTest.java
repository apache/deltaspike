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

import org.apache.deltaspike.core.impl.config.converter.CharacterConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 2.0.0
 */
public class CharacterConverterTest
{
    private CharacterConverter converter;

    @Before
    public void before()
    {
        converter = new CharacterConverter();
    }

    @Test
    public void testConvertingSingleCharacterString()
    {
        final char expected = 'a';
        final char actual = converter.convert("a");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertingSingleCharacterSpace()
    {
        final char expected = ' ';
        final char actual = converter.convert(" ");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertingSingleNumber()
    {
        final char expected = '1';
        final char actual = converter.convert("1");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertSymbol()
    {
        final char expected = '$';
        final char actual = converter.convert("$");

        Assert.assertEquals(expected, actual);
    }

    /**
     * If a hexadecimal value is provided, we'll convert it to a
     * a character if possible.
     */
    @Test
    public void testConvertHex()
    {
        final char expected = 'A';
        final char actual = converter.convert("0x41");

        Assert.assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyString()
    {
        converter.convert("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHelloWorld()
    {
        converter.convert("Hello, world!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhiteSpace()
    {
        converter.convert("  ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAa()
    {
        converter.convert("AA");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test11()
    {
        converter.convert("11");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test00()
    {
        converter.convert("00");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXx()
    {
        converter.convert("XX");
    }
}
