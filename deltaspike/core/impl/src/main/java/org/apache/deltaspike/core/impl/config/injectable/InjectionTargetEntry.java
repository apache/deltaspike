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
package org.apache.deltaspike.core.impl.config.injectable;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

class InjectionTargetEntry
{
    private final Type type;
    private final ConfigProperty configProperty;
    private final Annotation optionalCustomQualifier;

    InjectionTargetEntry(Type type, ConfigProperty configProperty, Annotation optionalCustomQualifier)
    {
        this.type = type;
        this.configProperty = configProperty;
        this.optionalCustomQualifier = optionalCustomQualifier;
    }

    Type getType()
    {
        return type;
    }

    ConfigProperty getConfigProperty()
    {
        return configProperty;
    }

    Annotation getCustomQualifier()
    {
        return optionalCustomQualifier;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        InjectionTargetEntry that = (InjectionTargetEntry) o;

        if (!configProperty.equals(that.configProperty))
        {
            return false;
        }
        if (optionalCustomQualifier != null ? !optionalCustomQualifier
                .equals(that.optionalCustomQualifier) : that.optionalCustomQualifier != null)
        {
            return false;
        }
        if (!type.equals(that.type))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = type.hashCode();
        result = 31 * result + configProperty.hashCode();
        result = 31 * result + (optionalCustomQualifier != null ? optionalCustomQualifier.hashCode() : 0);
        return result;
    }
}
