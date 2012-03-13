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
package org.apache.deltaspike.example.config;

import org.apache.deltaspike.core.api.converter.ConverterException;
import org.apache.deltaspike.core.api.converter.MetaDataAwareConverter;

import javax.enterprise.context.Dependent;
import java.util.logging.Logger;

@Dependent
public class CustomStringToLongConverter implements MetaDataAwareConverter<String, Long, Property2>
{
    private static final Logger LOG = Logger.getLogger(CustomStringToLongConverter.class.getName());

    @Override
    public Long convert(String source, Property2 metaData)
    {
        Long result = convert(source);

        if (metaData.logValue())
        {
            LOG.info("value of property 2: " + result);
        }

        return result;
    }

    @Override
    public Long convert(String source)
    {
        if (source == null || "".equals(source))
        {
            return 0L;
        }

        try
        {
            return Long.parseLong(source);
        }
        catch (NumberFormatException e)
        {
            throw new ConverterException(String.class, Long.class, e);
        }
    }
}
