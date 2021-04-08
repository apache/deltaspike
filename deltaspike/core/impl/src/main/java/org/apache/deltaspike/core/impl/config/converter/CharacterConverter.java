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

/**
 * <p>
 *     Converts a character or numeric value representing a character
 *     into a Java {@link Character} object.
 * </p>
 *
 * <p>
 *     This converter may also accept hexadecimal {@link String}s if obtaining
 *     a character from it's numeric value is desired.
 *
 *     This could be useful in cases where there are concerns regarding putting
 *     the actual character in it's raw form, such as system/file encodings between
 *     clients, applications, and servers.
 * </p>
 *
 * @since 1.9.5
 */
public class CharacterConverter implements ConfigResolver.Converter<Character>
{

    /** This prefix allows us to determine an input is a hexadecimal {@link String}. */
    private static final String HEX_PREFIX = "0x";

    /**
     * @param value The String property value to convert.
     * @return A {@link Character} which represents the configuration property value.
     * @throws NullPointerException If the value is null.
     * @throws IllegalArgumentException If an empty string is provided as the value.
     * @throws NumberFormatException If a hexadecimal {@link String} is provided, but
     * can not be parsed as an {@link Integer}.
     */
    @Override
    public Character convert(String value)
    {
        Objects.requireNonNull(value, "Value can't be null.");

        if (value.isEmpty())
        {
            throw new IllegalArgumentException("Value can't be empty.");
        }

        if (value.length() == 1)
        {
            return value.charAt(0);
        }

        if (value.toLowerCase().startsWith(HEX_PREFIX))
        {
            final String substring = value.substring(HEX_PREFIX.length());
            final int hex = Integer.parseInt(substring, 16);
            return (char)hex;
        }

        throw new IllegalArgumentException("Value can't be represented as a character.");
    }
}
