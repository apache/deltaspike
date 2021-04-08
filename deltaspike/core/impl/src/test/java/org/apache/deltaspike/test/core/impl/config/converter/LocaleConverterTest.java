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

import org.apache.deltaspike.core.impl.config.converter.LocaleConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

/**
 * @since 1.9.5
 */
public class LocaleConverterTest
{
    private LocaleConverter converter;

    @Before
    public void before()
    {
        converter = new LocaleConverter();
    }

    @Test
    public void testConvertStandardLocale()
    {
        final Locale expected = Locale.ENGLISH;
        final Locale actual = converter.convert("en");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConvertCustomLocale()
    {
        final Locale expected = Locale.forLanguageTag("en-owo");
        final Locale actual = converter.convert("en-owo");

        Assert.assertEquals(expected, actual);
    }
}
