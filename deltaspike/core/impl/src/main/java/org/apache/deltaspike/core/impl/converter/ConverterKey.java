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
package org.apache.deltaspike.core.impl.converter;

import javax.enterprise.inject.Typed;
import java.lang.annotation.Annotation;

@Typed()
class ConverterKey
{
    private final Class sourceType;
    private final Class targetType;
    private final Class<? extends Annotation> metaDataType;

    ConverterKey(Class sourceType, Class targetType, Class<? extends Annotation> metaDataType)
    {
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.metaDataType = metaDataType;
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

        ConverterKey that = (ConverterKey) o;

        if (metaDataType != null ? !metaDataType.equals(that.metaDataType) : that.metaDataType != null)
        {
            return false;
        }
        if (!sourceType.equals(that.sourceType))
        {
            return false;
        }
        if (!targetType.equals(that.targetType))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = sourceType.hashCode();
        result = 31 * result + targetType.hashCode();
        result = 31 * result + (metaDataType != null ? metaDataType.hashCode() : 0);
        return result;
    }
}
