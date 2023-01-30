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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.deltaspike.core.api.config.Config;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.util.ExceptionUtils;

/**
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 */
public class FieldInjectionBeanConverter<N> implements BiFunction<Config, String, N>
{
    private final Class<?> clazz;
    private final List<Field> fields;

    public <N> FieldInjectionBeanConverter(Class<N> clazz)
    {
        this.clazz = clazz;
        this.fields = collectFields(clazz);
    }

    private <N> List<Field> collectFields(Class<N> clazz)
    {
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() != Object.class)
        {
            fields.addAll(collectFields(clazz.getSuperclass()));
        }
        return fields;
    }

    @Override
    public N apply(Config config, String path)
    {
        try
        {
            final Object o = clazz.getDeclaredConstructor().newInstance();
            for (Field field : fields)
            {
                final ConfigProperty configProperty = field.getAnnotation(ConfigProperty.class);
                String name = configProperty != null ? configProperty.name() : field.getName();
                final ConfigResolver.UntypedResolver<String> resolver = config.resolve(path + name);
                if (field.getType() != String.class)
                {
                    resolver.as(field.getType());
                }
                resolver.evaluateVariables(configProperty != null ? configProperty.evaluateVariables() :  true);
                resolver.withCurrentProjectStage(configProperty != null ? configProperty.projectStageAware() :  true);
                if (!field.isAccessible())
                {
                    field.setAccessible(true);
                }
                field.set(o, resolver.getValue());
            }
            return (N) o;
        }
        catch (Exception e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }
}
