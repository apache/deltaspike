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

import java.awt.Color;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts a value into a {@link Color} object.
 *
 * <p>
 *     Will interpret hexadecimal colors similar to CSS engines, for example #RGB is interpreted as
 *     #RRGGBB. If using the literal hexadecimal value is desired, the value should be prefixed with <code>0x</code>
 *     instead of {@link #HEX_COLOR_PREFIX #}.
 * </p>
 *
 * @since 2.0.0
 */
public class ColorConverter implements ConfigResolver.Converter<Color>
{
    /** Prefix for hexadecimal color notation. */
    private static final String HEX_COLOR_PREFIX = "#";

    /** Regular expression matching the output of {@link Color#toString()}. */
    private static final Pattern JAVA_COLOR_PATTERN =
        Pattern.compile("^(?:[A-Za-z\\d._]+)??\\[?(?:r=)?(\\d{1,3}),(?:g=)?(\\d{1,3}),(?:b=)?(\\d{1,3})\\]?$");

    /**
     * Converts a {@link Color} into a {@link String}.
     *
     * <p>
     *     Supports hexadecimal colors like #RGB, #RRGGBB, #RGBA, and #RRGGBBAA, and interprets raw color names based
     *     on the colors defined in Java, such as:
     * </p>
     *
     * <ul>
     *     <li>{@link Color#BLACK}</li>
     *     <li>{@link Color#BLUE}</li>
     *     <li>{@link Color#CYAN}</li>
     *     <li>{@link Color#DARK_GRAY}</li>
     *     <li>{@link Color#GRAY}</li>
     *     <li>{@link Color#GREEN}</li>
     *     <li>{@link Color#LIGHT_GRAY}</li>
     *     <li>{@link Color#MAGENTA}</li>
     *     <li>{@link Color#ORANGE}</li>
     *     <li>{@link Color#PINK}</li>
     *     <li>{@link Color#RED}</li>
     *     <li>{@link Color#WHITE}</li>
     *     <li>{@link Color#YELLOW}</li>
     * </ul>
     *
     * @param value Value to convert.
     * @return {@link Color} which represents the compiled value.
     * @throws NullPointerException If the value is null.
     * @throws NumberFormatException If an invalid number is provided.
     */
    @Override
    public Color convert(String value)
    {
        Objects.requireNonNull(value, "Value must not be null.");

        switch (value.toLowerCase(Locale.ROOT))
        {
            case "black":
                return Color.BLACK;
            case "blue":
                return Color.BLUE;
            case "cyan":
                return Color.CYAN;
            case "darkgray":
            case "darkgrey":
            case "dark_gray":
            case "dark_grey":
                return Color.DARK_GRAY;
            case "gray":
            case "grey":
                return Color.GRAY;
            case "green":
                return Color.GREEN;
            case "lightgray":
            case "lightgrey":
            case "light_gray":
            case "light_grey":
                return Color.LIGHT_GRAY;
            case "magenta":
                return Color.MAGENTA;
            case "orange":
                return Color.ORANGE;
            case "pink":
                return Color.PINK;
            case "red":
                return Color.RED;
            case "white":
                return Color.WHITE;
            case "yellow":
                return Color.YELLOW;
            default:
                // Do nothing.
        }

        if (value.startsWith(HEX_COLOR_PREFIX))
        {
            return parseWebColor(value);
        }

        if (value.contains(","))
        {
            return parseToStringColor(value);
        }

        return Color.decode(value);
    }

    /**
     * Parses the Color based on the result of the {@link Color#toString()} method.
     *
     * Accepts the following values:
     * <ul>
     *     <li><code>java.awt.Color[r=255,g=255,b=255]</code></li>
     *     <li><code>[r=255,g=255,b=255]</code></li>
     *     <li><code>r=255,g=255,b=255</code></li>
     *     <li><code>255,255,255</code></li>
     * </ul>
     *
     * @param value A color as represented by {@link Color#toString()}.
     * @return The Java friendly {@link Color} this color represents.
     * @throws IllegalArgumentException If the input can't be matched by the {@link #JAVA_COLOR_PATTERN}
     * or a {@link Color} component specified is over 255.
     */
    private Color parseToStringColor(final String value)
    {
        Objects.requireNonNull(value);

        Matcher matcher = JAVA_COLOR_PATTERN.matcher(value);

        if (!matcher.matches())
        {
            throw new IllegalArgumentException("Invalid Color String provided. Could not parse.");
        }

        final int red = Integer.parseInt(matcher.group(1));
        final int green = Integer.parseInt(matcher.group(2));
        final int blue = Integer.parseInt(matcher.group(3));

        if (red > 255 || green > 255 || blue > 255)
        {
            throw new IllegalArgumentException("Color component integers must be between 0 and 255.");
        }

        return new Color(red, green, blue);
    }

    /**
     * Returns a {@link Color} for hexadecimal colors.
     *
     * @param value Hexadecimal representation of a color.
     * @return The converted value.
     * @throws NumberFormatException If the hexadecimal input contains non parsable characters.
     */
    public Color parseWebColor(final String value)
    {
        Objects.requireNonNull(value);

        switch (value.length())
        {
            case 4:
                return new Color(
                    Integer.parseInt(value.substring(1, 2), 16) * 17,
                    Integer.parseInt(value.substring(2, 3), 16) * 17,
                    Integer.parseInt(value.substring(3, 4), 16) * 17
                );
            case 5:
                return new Color(
                    Integer.parseInt(value.substring(1, 2), 16) * 17,
                    Integer.parseInt(value.substring(2, 3), 16) * 17,
                    Integer.parseInt(value.substring(3, 4), 16) * 17,
                    Integer.parseInt(value.substring(4, 5), 16) * 17
                );
            case 7:
                return new Color(
                    Integer.parseInt(value.substring(1, 3), 16),
                    Integer.parseInt(value.substring(3, 5), 16),
                    Integer.parseInt(value.substring(5, 7), 16)
                );
            case 9:
                return new Color(
                    Integer.parseInt(value.substring(1, 3), 16),
                    Integer.parseInt(value.substring(3, 5), 16),
                    Integer.parseInt(value.substring(5, 7), 16),
                    Integer.parseInt(value.substring(7, 9), 16)
                );
            default:
                throw new IllegalArgumentException("Value is an malformed hexadecimal color, if literal value decoding " +
                    "is required, prefix with 0x instead of #, otherwise expecting 3, 4, 6, or 8 characters only.");
        }
    }
}
