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
package org.apache.deltaspike.core.api.config.base;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;

import java.lang.reflect.Method;

public class TypedConfig<T>
{
    protected final String key;
    protected final T defaultValue;
    protected final Class<? extends T> configEntryType;

    //currently just needed to make this helper more useful for users
    protected final Object customTypeConverter;
    protected final Method converterMethod;

    public TypedConfig(String key, T defaultValue)
    {
        this(key, defaultValue, null, defaultValue != null ? (Class<T>)defaultValue.getClass() : null);
    }

    public TypedConfig(String key, T defaultValue, Object customTypeConverter)
    {
        this(key, defaultValue, customTypeConverter, defaultValue != null ? (Class<T>)defaultValue.getClass() : null);
    }

    public TypedConfig(String key, T defaultValue, Class<T> targetType)
    {
        this(key, defaultValue, null, targetType);
    }

    public TypedConfig(String key, T defaultValue, Object customTypeConverter, Class<T> targetType)
    {
        if (defaultValue == null && targetType == null)
        {
            throw new IllegalArgumentException("'null' isn't supported by this helper for " +
                "the default-value and target-type");
        }
        this.key = key;
        this.defaultValue = defaultValue;
        this.configEntryType = getConfigEntryType(targetType, defaultValue);

        this.customTypeConverter = customTypeConverter;
        this.converterMethod = getCustomTypeConverterMethod(customTypeConverter);
        validateConfigEntryType();
    }

    protected Class<? extends T> getConfigEntryType(Class<T> targetType, T defaultValue)
    {
        if (targetType != null)
        {
            return targetType;
        }
        return (Class<? extends T>)defaultValue.getClass();
    }

    protected Method getCustomTypeConverterMethod(Object customTypeConverter)
    {
        Method foundConverterMethod = null;
        if (customTypeConverter != null)
        {
            for (Method currentMethod : customTypeConverter.getClass().getDeclaredMethods())
            {
                if (currentMethod.getParameterTypes().length == 1 &&
                    currentMethod.getParameterTypes()[0].equals(String.class) &&
                    currentMethod.getReturnType().equals(this.configEntryType))
                {
                    foundConverterMethod = currentMethod;
                    break;
                }
            }
        }

        return foundConverterMethod;
    }

    protected void validateConfigEntryType()
    {
        //same types as supported by DefaultConfigPropertyProducer
        if (this.converterMethod == null &&
            !(this.configEntryType.equals(String.class) ||
                this.configEntryType.equals(Class.class) ||
                this.configEntryType.equals(Boolean.class) ||
                this.configEntryType.equals(Integer.class) ||
                this.configEntryType.equals(Float.class)))
        {
            throw new IllegalArgumentException(
                    this.configEntryType.getName() + " isn't supported out-of-the-box and" +
                            "no valid (custom) type-converter can be found");
        }
    }

    public String getKey()
    {
        return key;
    }

    public T getDefaultValue()
    {
        return defaultValue;
    }

    public T getValue()
    {
        String resultString = ConfigResolver.getPropertyValue(
            this.key, this.defaultValue != null ? this.defaultValue.toString() : null);

        Object result = null;

        if (resultString == null)
        {
            return null;
        }

        //same types as supported by DefaultConfigPropertyProducer
        else if (String.class.equals(this.configEntryType))
        {
            result = resultString;
        }
        else if (Class.class.equals(this.configEntryType))
        {
            result = ClassUtils.tryToLoadClassForName(resultString);
        }
        else if (Boolean.class.equals(this.configEntryType))
        {
            result = Boolean.valueOf(resultString);
        }
        else if (Integer.class.equals(this.configEntryType))
        {
            result = Integer.parseInt(resultString);
        }
        else if (Float.class.equals(this.configEntryType))
        {
            result = Float.parseFloat(resultString);
        }
        else if (this.customTypeConverter != null)
        {
            try
            {
                result = this.converterMethod.invoke(this.customTypeConverter, resultString);
            }
            catch (Exception e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }

        return (T)result;
    }
}
