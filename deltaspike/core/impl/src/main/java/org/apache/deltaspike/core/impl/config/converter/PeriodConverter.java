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
import java.util.Objects;

/**
 * Converts a period of time to the {@link Period} type.
 *
 * <p>
 *     Accepts the following:
 *     <ul>
 *         <li>
 *             {@link Period} {@link String} format, for example
 *             <code>P2Y</code> (2 {@link java.time.temporal.ChronoUnit#YEARS}).
 *         </li>
 *     </ul>
 * </p>
 *
 * @since 2.0.0
 */
public class PeriodConverter implements ConfigResolver.Converter<Period>
{
    /**
     * @param value Value to convert.
     * @return The value represented as a{@link Period}.
     * @throws NullPointerException If the value is null.
     */
    @Override
    public Period convert(String value)
    {
        Objects.requireNonNull(value, "Value must not be null.");
        return Period.parse(value);
    }
}
