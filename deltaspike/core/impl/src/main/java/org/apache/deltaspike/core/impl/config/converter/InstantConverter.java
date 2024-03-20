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
import java.util.Objects;

/**
 * Converter to parse the time using the {@link java.time.format.DateTimeFormatter#ISO_INSTANT}
 * format.
 *
 * @since 2.0.0
 */
public class InstantConverter implements ConfigResolver.Converter<Instant>
{
    /**
     * @param value Value to convert.
     * @return An {@link Instant} which represents the value.
     * @throws NullPointerException If the value is null.
     * @throws DateTimeParseException If the value can't be parsed as an {@link Instant}.
     */
    @Override
    public Instant convert(String value)
    {
        Objects.requireNonNull(value, "Value must not be null.");
        return Instant.parse(value);
    }
}
