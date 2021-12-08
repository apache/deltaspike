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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.deltaspike.core.api.config.Config;

/**
 * A factory for bean converters.
 */
public final class BeanConverterFactory
{
    private final ConcurrentMap<Class<?>, BiFunction<Config, String, ?>> beanConverters = new ConcurrentHashMap<>();

    /**
     * Determine the bean converter function to be used according to the rules defined in
     * {@link org.apache.deltaspike.core.api.config.ConfigResolver.UntypedResolver#asBean(Class)}
     */
    public <N> BiFunction<Config, String, N> detectConverter(Class<N> clazz)
    {
        BiFunction<Config, String, N> beanConverter = (BiFunction<Config, String, N>) beanConverters.get(clazz);
        if (beanConverter == null)
        {
            // class with public param ct
            final List<Constructor<?>> paramConstructors = Arrays.stream(clazz.getConstructors())
                .filter(ct -> ct.getParameterTypes().length > 0)
                .collect(Collectors.toList());
            if (paramConstructors.size() > 1)
            {
                throw new IllegalStateException("Cannot handle beans with multiple non-default ct");
            }
            if (paramConstructors.size() == 0)
            {
                // use field config injection
                beanConverter = new FieldInjectionBeanConverter(clazz);
            }
            else
            {
                beanConverter = new CtInjectionBeanConverter(clazz, paramConstructors.get(0));
            }

            beanConverters.putIfAbsent(clazz, beanConverter);
        }

        return beanConverter;
    }

}
