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

import org.apache.deltaspike.core.impl.config.converter.PatternConverter;
import org.apache.deltaspike.core.impl.config.converter.PointConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.Point;

/**
 * @since 1.9.5
 */
public class PointConverterTest
{
    private PointConverter converter;

    @Before
    public void before()
    {
        converter = new PointConverter();
    }

    @Test
    public void testConverteringPoint()
    {
        final Point expected = new Point(100, 200);
        final Point actual = converter.convert("(100, 200)");

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConverteringNoSpace()
    {
        final Point expected = new Point(100, 200);
        final Point actual = converter.convert("(100,200)");

        Assert.assertEquals(expected, actual);
    }
}
