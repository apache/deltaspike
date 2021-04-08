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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts url to a Java {@link URL} instance.
 * This also can be extended to support filtering of URL components
 * such as {@link URL#getProtocol()} for more specialized use or validation.
 *
 * @since 1.9.5
 */
public class UrlConverter implements ConfigResolver.Converter<URL>
{

    /** A list of allowed protocols, or null to disable this. */
    private final Set<String> allowedProtocols;

    public UrlConverter()
    {
        this(null);
    }

    /**
     * @param allowedProtocols An array of allowed protocols, or null to disable checking.
     */
    public UrlConverter(Collection<String> allowedProtocols)
    {
        if (allowedProtocols != null)
        {
            this.allowedProtocols = allowedProtocols.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        }
        else
        {
            this.allowedProtocols = null;
        }
    }

    /**
     * @param value The String property value to convert.
     * @return A {@link URL} which represents the configuration property value.
     * @throws NullPointerException If the value is null.
     * @throws IllegalArgumentException If the value is not a valid {@link URL}.
     */
    @Override
    public URL convert(String value)
    {
        Objects.requireNonNull(value, "Value can't be null.");

        try
        {
            URL url = new URL(value);

            if (allowedProtocols != null)
            {
                String protocol = url.getProtocol().toLowerCase();

                if (!allowedProtocols.contains(protocol))
                {
                    throw new IllegalArgumentException("URL provided must specify one of the following protocols: " +
                        String.join(", ", allowedProtocols));
                }
            }

            return url;
        }
        catch (MalformedURLException ex)
        {
            throw new IllegalArgumentException(
                "Configuration value provided is not a valid URL, or uses an unknown protocol.", ex
            );
        }
    }
}
