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

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * This will attempt to parse the time using the {@link java.time.format.DateTimeFormatter#ISO_INSTANT}
 * format. If this isn't possible we'll read the value as a literal {@link Long} so
 * it can be interpretted as unix time.
 *
 * @since 1.9.5
 * @see <a href="https://en.wikipedia.org/wiki/Unix_time">Unix Time on Wikipedia</a>
 */
public class InstantConverter implements ConfigResolver.Converter<Instant>
{

    /**
     * @param value The String property value to convert.
     * @return An {@link Instant} which represents the configuration property value.
     * @throws NullPointerException If the value is null.
     * @throws IllegalArgumentException If unable to convert the value to an {@link Instant}.
     */
    @Override
    public Instant convert(String value)
    {
        Objects.requireNonNull(value, "Value can't be null.");

        try
        {
            return Instant.parse(value);
        }
        catch (DateTimeParseException ex)
        {
            try
            {
                final long epoch = Long.parseLong(value);
                return Instant.ofEpochMilli(epoch);
            }
            catch (RuntimeException rEx)
            {
                throw new IllegalArgumentException("Value isn't a valid DateTimeFormatter.ISO_INSTANT String, " +
                    "and isn't a valid number of milliseconds. " +
                    "Unable to convert value to Instant.", rEx);
            }
        }
    }
}
