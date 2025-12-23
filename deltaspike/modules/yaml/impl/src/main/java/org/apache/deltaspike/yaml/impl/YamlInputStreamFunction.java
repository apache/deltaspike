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
package org.apache.deltaspike.yaml.impl;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java requires super be the first call of a method; we use this {@link Function} to perform more methods calls while calling super.
 *
 * @since 2.0.1
 */
public class YamlInputStreamFunction implements Function<InputStream, Map<String, Object>>
{
    private final Logger log = Logger.getLogger(YamlInputStreamFunction.class.getName());

    /**
     * @param inputStream Input stream to read the YAML configuration from.
     * @return Nested map representing all YAML properties.
     */
    @Override
    public Map<String, Object> apply(InputStream inputStream)
    {
        if (inputStream != null)
        {
            return new Yaml().load(inputStream);
        }

        log.log(Level.WARNING, "Using YamlConfigSource, but the stream was null.");
        return new HashMap<>();
    }
}
