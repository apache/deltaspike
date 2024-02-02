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
import java.util.regex.Pattern;

/**
 * Converts a regular expression into a Java {@link Pattern} object.
 *
 * @since 2.0.0
 */
public class PatternConverter implements ConfigResolver.Converter<Pattern>
{
    /**
     * @param value Value to convert.
     * @return A {@link Pattern} which represents the compiled value.
     * @throws NullPointerException If the value is null.
     * @throws PatternSyntaxException If the pattern is malformed.
     */
    @Override
    public Pattern convert(String value)
    {
        Objects.requireNonNull(value, "Value must not be null.");
        return Pattern.compile(value);
    }
}
