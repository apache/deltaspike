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
package org.apache.deltaspike.core.impl.config.converter;

import org.apache.deltaspike.core.api.config.ConfigResolver;

import java.awt.Dimension;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts values to a {@link Dimension}.
 *
 * <p>
 *     Accepts a single {@link Integer} value, or two {@link Integer} values separated by the character <code>x</code>.
 * </p>
 *
 * <p>The dimensions must consist of non-negative {@ink Integer} values.</p>
 *
 * @since 2.0.0
 */
public class DimensionConverter implements ConfigResolver.Converter<Dimension>
{
    /** Pattern to validate and tokenize the {@link String}. */
    private static final Pattern DIMENSION_PATTERN = Pattern.compile("(\\d+)(?:x(\\d+))?");

    /**
     * @param value Value to convert.
     * @return A {@link Dimension} which represents the value.
     * @throws NullPointerException If the value is null.
     * @throws NumberFormatException If the {@link Dimension} width or height is bigger than {@link Integer#MAX_VALUE}.
     */
    @Override
    public Dimension convert(final String value)
    {
        Objects.requireNonNull(value, "Dimensions can not be null.");

        if (value.isEmpty())
        {
            throw new IllegalArgumentException("Dimensions can not be empty.");
        }

        Matcher matcher = DIMENSION_PATTERN.matcher(value);

        if (!matcher.matches())
        {
            throw new IllegalArgumentException("Dimension must match format: {width/height} or {width}x{height}");
        }

        String x = matcher.group(1);
        String y = matcher.group(2);

        int xValue = Integer.parseInt(x);
        int yValue = (y == null || x.equals(y)) ? xValue : Integer.parseInt(y);

        return new Dimension(xValue, yValue);
    }
}
