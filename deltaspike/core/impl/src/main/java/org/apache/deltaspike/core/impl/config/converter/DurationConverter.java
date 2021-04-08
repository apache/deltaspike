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

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

/**
 * <p>Converts a duration of time to the {@link Duration} type.</p>
 *
 * Accepts the following:
 * <ul>
 *     <li>{@link Duration} {@link String} format, for example
 *     <code>PT15M</code> (15 {@link ChronoUnit#MINUTES}).</li>
 *     <li>{@link Long} number of {@link ChronoUnit#MILLIS}, for example
 *     <code>5000</code> (5000 {@link ChronoUnit#MILLIS}).</li>
 * </ul>
 *
 * @since 1.9.5
 */
public class DurationConverter implements ConfigResolver.Converter<Duration>
{

    /** If a literal number is provided, parse it as this {@link TemporalUnit}. */
    private final TemporalUnit unit;

    /**
     * Constructs the {@link DurationConverter} using {@link ChronoUnit#MILLIS} as
     * the default {@link TemporalUnit} if a literal {@link Number} is provided.
     */
    public DurationConverter()
    {
        this(ChronoUnit.MILLIS);
    }

    /**
     * @param unit The unit to use if a literal {@link Number} is provided
     * rather than a {@link Duration} {@link String}.
     */
    public DurationConverter(final TemporalUnit unit)
    {
        this.unit = Objects.requireNonNull(unit);
    }

    /**
     * @param value The String property value to convert.
     * @return A {@link Duration} which represents the configuration property value.
     * @throws NullPointerException If the value is null.
     * @throws IllegalArgumentException If the value is not a valid {@link Duration} {@link String},
     * and isn't a valid {@link Long} value to be used with the defined {@link #unit}.
     */
    @Override
    public Duration convert(String value)
    {
        Objects.requireNonNull(value, "Value can't be null.");

        try
        {
            return Duration.parse(value);
        }
        catch (DateTimeParseException ex)
        {
            try
            {
                long number = Long.parseLong(value);
                return Duration.of(number, unit);
            }
            catch (RuntimeException rEx)
            {
                throw new IllegalArgumentException("Value provided is not a valid duration String, " +
                    "or valid Long value and can't be converted to a Duration.", rEx);
            }
        }
    }
}
