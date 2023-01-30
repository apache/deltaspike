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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.deltaspike.core.api.config.Config;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.util.ExceptionUtils;

/**
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
public class CtInjectionBeanConverter<N> implements BiFunction<Config, String, N>
{
    private final Constructor<?> constructor;

    public <N> CtInjectionBeanConverter(Class<N> clazz, Constructor<?> constructor)
    {
        this.constructor = constructor;
    }

    @Override
    public N apply(Config config, String path)
    {
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < constructor.getParameters().length; i++)
        {
            Parameter p = constructor.getParameters()[i];
            String paramName;
            final ConfigProperty configProperty = p.getAnnotation(ConfigProperty.class);
            if (configProperty != null)
            {
                paramName = configProperty.name();
            }
            else
            {
                paramName = p.getName();
                if (paramName.equals("arg" + i))
                {
                    throw new IllegalStateException("Config POJO constructor pareameters must be annotated with @ConfigProperty if the " +
                        "class is not compiled with the javac -parameters option!");
                }
            }

            params.add(config.resolve(path + paramName)
                .as(p.getType())
                .getValue());
        }

        if (params.stream().allMatch(p -> p == null))
        {
            return null;
        }

        try
        {
            return (N) constructor.newInstance(params.toArray(new Object[params.size()]));
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException  e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }
}
