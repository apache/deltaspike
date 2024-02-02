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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts an {@link Enum} {@link String} into a Java {@link Enum} object.
 *
 * <p>
 *     This accepts values such as:
 *     <ul>
 *         <li>
 *             <code>java.util.concurrent.TimeUnit.NANOSECONDS</code>
 *         </li>
 *         <li>
 *             <code>java.time.DayOfWeek#MONDAY</code>
 *         </li>
 *     </ul>
 * </p>
 *
 * @since 2.0.0
 */
public class EnumConverter implements ConfigResolver.Converter<Enum>
{

    /** Matches if a given input is an enum string. */
    private static final Pattern ENUM_PATTERN = Pattern.compile(
        "((?:[a-z\\d.]+)*)\\.([A-Za-z\\d]+)[#.]([A-Z\\d_]+)"
    );

    /** The enum type required by the converter. */
    private final Class<? extends Enum> requiredEnumType;

    /**
     * Initialize the {@link EnumConverter} to accept any {@link Enum} constant.
     */
    public EnumConverter()
    {
        this(Enum.class);
    }

    /**
     * Initialize the {@link EnumConverter} to accept enum constants for the specific enum type.
     *
     * It's possible to use this by extending this class and calling the super constructor, for example:
     *
     * <pre><code>
     * public class TimeUnitConverter extends EnumConverter
     * {
     *
     *     public TimeUnitConverter()
     *     {
     *         super(TimeUnit.class);
     *     }
     * }
     * </code></pre>
     *
     * <p>
     *     This allows the use of the non-qualified {@link Enum} constant name instead,
     *     and provides validation of the type of enum loaded, so an {@link IllegalArgumentException}
     *     can be thrown instead of a {@link ClassCastException}.
     * </p>
     *
     * @param requiredEnumType An enum class, such as {@link java.util.concurrent.TimeUnit},
     * or {@link java.time.DayOfWeek}.
     */
    public EnumConverter(Class<? extends Enum> requiredEnumType)
    {
        this.requiredEnumType = Objects.requireNonNull(requiredEnumType);
    }

    /**
     * @param value Value to convert.
     * @return A {@link Enum} constant which represents the value.
     * @throws NullPointerException If the value is null.
     * @throws IllegalArgumentException If the value can not be mapped to an {@link Enum} constant.
     */
    @Override
    public Enum convert(String value)
    {
        Objects.requireNonNull(value, "Value must not be null.");

        if (requiredEnumType != null)
        {
            try
            {
                return Enum.valueOf(requiredEnumType, value);
            }
            catch (IllegalArgumentException ex)
            {
                // Continue to check fully qualified name.
            }
        }

        Matcher matcher = ENUM_PATTERN.matcher(value);

        if (!matcher.matches())
        {
            throw new IllegalArgumentException("Value does not follow Java naming conventions, expecting input " +
                "like: java.time.DayOfWeek.MONDAY");
        }

        try
        {
            String className = matcher.group(1) + "." + matcher.group(2);
            Class type = Class.forName(className);

            if (!type.isEnum())
            {
                throw new IllegalArgumentException("Value provided isn't an enumerated type.");
            }

            if (!requiredEnumType.isAssignableFrom(type))
            {
                throw new IllegalArgumentException("Class provided is not the required type.");
            }

            return Enum.valueOf(type, matcher.group(3));
        }
        catch (ClassNotFoundException ex)
        {
            throw new IllegalArgumentException("Class specified doesn't exist.", ex);
        }
    }
}
