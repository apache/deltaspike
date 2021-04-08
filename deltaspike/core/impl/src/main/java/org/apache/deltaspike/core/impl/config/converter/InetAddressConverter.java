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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * For converting configuration property values to an IP address.
 *
 * @since 1.9.5
 * @see <a href="https://en.wikipedia.org/wiki/Inet_address">IP Address on Wikipedia</a>
 */
public class InetAddressConverter implements ConfigResolver.Converter<InetAddress>
{

    /**
     * @param value The String property value to convert.
     * @return An {@link InetAddress} which represents the configuration property value.
     * @throws NullPointerException If the value is null.
     * @throws IllegalArgumentException If a host name was specified and the IP address couldn't be obtained.
     */
    @Override
    public InetAddress convert(String value)
    {
        Objects.requireNonNull(value, "Value can't be null.");

        try
        {
            return InetAddress.getByName(value);
        }
        catch (UnknownHostException ex)
        {
            throw new IllegalArgumentException("Unable to get IP address of the named host.", ex);
        }
    }
}
