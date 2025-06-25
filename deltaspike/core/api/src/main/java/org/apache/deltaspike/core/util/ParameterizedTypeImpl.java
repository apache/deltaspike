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

package org.apache.deltaspike.core.util;

import jakarta.enterprise.inject.Vetoed;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * A basic implementation of {@link ParameterizedType}.
 */
@Vetoed
class ParameterizedTypeImpl implements ParameterizedType
{
    private final Type[] actualTypeArguments;
    private final Type rawType;
    private final Type ownerType;

    ParameterizedTypeImpl(Type rawType, Type[] actualTypeArguments, Type ownerType)
    {
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
        this.ownerType = ownerType;
    }

    public Type[] getActualTypeArguments()
    {
        return Arrays.copyOf(actualTypeArguments, actualTypeArguments.length);
    }

    public Type getOwnerType()
    {
        return ownerType;
    }

    public Type getRawType()
    {
        return rawType;
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(actualTypeArguments) ^ (ownerType == null ? 0 :
                ownerType.hashCode()) ^ (rawType == null ? 0 : rawType.hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof ParameterizedType)
        {
            ParameterizedType that = (ParameterizedType) obj;
            Type thatOwnerType = that.getOwnerType();
            Type thatRawType = that.getRawType();
            return (ownerType == null ? thatOwnerType == null : ownerType.equals(thatOwnerType))
                    && (rawType == null ? thatRawType == null : rawType.equals(thatRawType))
                    && Arrays.equals(actualTypeArguments, that.getActualTypeArguments());
        }
        else
        {
            return false;
        }

    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(rawType);
        if (actualTypeArguments.length > 0)
        {
            sb.append("<");
            for (Type actualType : actualTypeArguments)
            {
                sb.append(actualType);
                sb.append(",");
            }
            sb.delete(sb.length() - 1, sb.length());
            sb.append(">");
        }
        return sb.toString();
    }
}
