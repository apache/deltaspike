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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Converts a {@link String} to a Java {@link URI} instance.
 *
 * @since 2.0.0
 */
public class UriConverter implements ConfigResolver.Converter<URI>
{
    /**
     * @param value Value to convert.
     * @return A {@link URI} which represents the value.
     * @throws NullPointerException If the value is null.
     * @throws IllegalArgumentException If the value is not a valid {@link URI}.
     */
    @Override
    public URI convert(String value)
    {
        Objects.requireNonNull(value, "Value must not be null.");

        if (value.isEmpty())
        {
            throw new IllegalArgumentException("Value must not be empty.");
        }

        try
        {
            return new URI(value);
        }
        catch (URISyntaxException ex)
        {
            throw new IllegalArgumentException("Value must be a valid URI.", ex);
        }
    }
}
