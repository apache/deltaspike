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
import java.time.temporal.TemporalUnit;
import java.util.Objects;

/**
 * Converts a duration of time to the {@link Duration} type.
 *
 * @since 2.0.0
 */
public class DurationConverter implements ConfigResolver.Converter<Duration>
{
    /** If a literal number is provided, parse it as this {@link TemporalUnit}. */
    private final TemporalUnit unit;

    public DurationConverter()
    {
        this(null);
    }

    /**
     * @param unit The unit to use if a literal {@link Number} is provided
     * rather than a {@link Duration} {@link String}.
     */
    public DurationConverter(final TemporalUnit unit)
    {
        this.unit = unit;
    }

    /**
     * @param value Value to convert.
     * @return A {@link Duration} which represents the value.
     * @throws NullPointerException If the value is null.
     * @throws IllegalArgumentException If the value is not a valid {@link Duration} {@link String},
     * and isn't a valid {@link Long} value to be used with the defined {@link #unit}.
     */
    @Override
    public Duration convert(String value)
    {
        Objects.requireNonNull(value, "Value must not be null.");

        try
        {
            return Duration.parse(value);
        }
        catch (DateTimeParseException ex)
        {
            if (unit != null)
            {
                long number = Long.parseLong(value);
                return Duration.of(number, unit);
            }

            throw new IllegalArgumentException("Value is not a valid duration String.");
        }
    }
}
