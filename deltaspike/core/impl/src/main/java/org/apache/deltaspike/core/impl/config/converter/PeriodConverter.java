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

import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * <p>Converts a period of time to the {@link Period} type.</p>
 *
 * Accepts the following:
 * <ul>
 *     <li>
 *         {@link Period} {@link String} format, for example
 *         <code>P2Y</code> (2 {@link java.time.temporal.ChronoUnit#YEARS}).
 *     </li>
 *     <li>
 *         {@link Integer} number of {@link java.time.temporal.ChronoUnit#DAYS}, for example
 *         <code>31</code> (31 {@link java.time.temporal.ChronoUnit#DAYS}).
 *     </li>
 * </ul>
 *
 * @since 1.9.5
 */
public class PeriodConverter implements ConfigResolver.Converter<Period>
{

    /**
     * @param value The String property value to convert.
     * @return A value provided, converting to a {@link Period} of time instead.
     * @throws NullPointerException If the value is null.
     * @throws IllegalArgumentException If the value is not a valid {@link Period} {@link String},
     * and isn't a valid {@link Integer} to represent {@link java.time.temporal.ChronoUnit#DAYS}.
     */
    @Override
    public Period convert(String value)
    {
        Objects.requireNonNull(value, "Value can't be null.");

        try
        {
            return Period.parse(value);
        }
        catch (DateTimeParseException ex)
        {
            try
            {
                int number = Integer.parseInt(value);
                return Period.ofDays(number);
            }
            catch (RuntimeException rEx)
            {
                throw new IllegalArgumentException("Value provided is not a valid Period String, or valid Integer " +
                    "value and can't be converted to a Period.", rEx);
            }
        }
    }
}
