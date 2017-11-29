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
package org.apache.deltaspike.core.impl.config;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.Configuration;
import org.apache.deltaspike.core.spi.config.BaseConfigPropertyProducer;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class ProxyConfigurationLifecycle implements ContextualLifecycle
{
    private Class<?>[] api;

    ProxyConfigurationLifecycle(final Class<?> proxyType)
    {
        this.api = new Class<?>[]{proxyType};
    }

    @Override
    public Object create(final Bean bean, final CreationalContext creationalContext)
    {
        // TODO: support partialbean binding? can make sense for virtual properties + would integrate with jcache
        // we'd need to add @PartialBeanBinding on a bean created from ConfigurationHandler
        // detection can just be a loadClass of this API
        // for now: waiting for user request for it

        final Configuration configuration = api[0].getAnnotation(Configuration.class);
        final long cacheFor = configuration.cacheFor();
        return Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(), api,
                new ConfigurationHandler(
                        cacheFor <= 0 ? -1 : configuration.cacheUnit().toMillis(cacheFor), configuration.prefix()));
    }

    @Override
    public void destroy(final Bean bean, final Object instance, final CreationalContext creationalContext)
    {
        // no-op
    }

    private static final class ConfigurationHandler implements InvocationHandler
    {
        private final BaseConfigPropertyProducer delegate = new BaseConfigPropertyProducer()
        {
        };

        private final ConcurrentMap<Method, Supplier<?>> resolvers =
                new ConcurrentHashMap<Method, Supplier<?>>();
        private final long cacheMs;
        private final String prefix;

        private ConfigurationHandler(final long cacheMs, final String prefix)
        {
            this.cacheMs = cacheMs;
            this.prefix = prefix;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
        {
            if (Object.class == method.getDeclaringClass())
            {
                try
                {
                    return method.invoke(this, args);
                }
                catch (final InvocationTargetException ite)
                {
                    throw ite.getCause();
                }
            }

            Supplier<?> supplier = resolvers.get(method);
            if (supplier == null)
            {
                final ConfigProperty annotation = method.getAnnotation(ConfigProperty.class);
                if (annotation == null)
                {
                    throw new UnsupportedOperationException(
                            method + " doesn't have @ConfigProperty and therefore is illegal");
                }

                // handle primitive bridge there (cdi doesnt support primitives but no reason our proxies don't)
                final Class<? extends ConfigResolver.Converter> converter = annotation.converter();

                final Type genericReturnType = method.getGenericReturnType();
                Class<?> returnType = method.getReturnType();
                final boolean list;
                final boolean set;
                if (converter == ConfigResolver.Converter.class &&
                        ParameterizedType.class.isInstance(genericReturnType))
                {
                    ParameterizedType pt = ParameterizedType.class.cast(genericReturnType);
                    if (List.class == pt.getRawType() && pt.getActualTypeArguments().length == 1)
                    {
                        list = true;
                        set = false;
                        final Type arg = pt.getActualTypeArguments()[0];
                        if (Class.class.isInstance(arg))
                        {
                            returnType = Class.class.cast(arg);
                        }
                    }
                    else if (Set.class == pt.getRawType() && pt.getActualTypeArguments().length == 1)
                    {
                        list = false;
                        set = true;
                        final Type arg = pt.getActualTypeArguments()[0];
                        if (Class.class.isInstance(arg))
                        {
                            returnType = Class.class.cast(arg);
                        }
                    }
                    else
                    {
                        list = false;
                        set = false;
                    }
                }
                else
                {
                    list = false;
                    set = false;

                    if (int.class == returnType)
                    {
                        returnType = Integer.class;
                    }
                    else if (long.class == returnType)
                    {
                        returnType = Long.class;
                    }
                    else if (boolean.class == returnType)
                    {
                        returnType = Boolean.class;
                    }
                    else if (short.class == returnType)
                    {
                        returnType = Short.class;
                    }
                    else if (byte.class == returnType)
                    {
                        returnType = Byte.class;
                    }
                    else if (float.class == returnType)
                    {
                        returnType = Float.class;
                    }
                    else if (double.class == returnType)
                    {
                        returnType = Double.class;
                    }
                }

                final String defaultValue = annotation.defaultValue();
                ConfigResolver.TypedResolver<?> typedResolver = delegate.asResolver(
                        prefix + annotation.name(), list || set ? ConfigProperty.NULL : defaultValue,
                        returnType, converter, annotation.parameterizedBy(),
                        annotation.projectStageAware(), annotation.evaluateVariables());

                if (cacheMs > 0)
                {
                    typedResolver.cacheFor(MILLISECONDS, cacheMs);
                }

                if (list || set)
                {
                    ConfigResolver.TypedResolver<? extends List<?>> listTypedResolver = typedResolver.asList();
                    final ConfigResolver.TypedResolver<? extends List<?>> resolver;
                    if (!ConfigProperty.NULL.equals(defaultValue))
                    {
                        resolver = listTypedResolver.withStringDefault(defaultValue);
                    }
                    else
                    {
                        resolver = listTypedResolver;
                    }

                    if (list)
                    {
                        supplier = new DefaultSupplier(resolver);
                    }
                    else
                    {
                        supplier = new Supplier<Set<?>>()
                        {
                            @Override
                            public Set<?> get()
                            {
                                return new HashSet(resolver.getValue());
                            }
                        };
                    }
                }
                else
                {
                    supplier = new DefaultSupplier(typedResolver);
                }

                final Supplier<?> existing = resolvers.putIfAbsent(method, supplier);
                if (existing != null)
                {
                    supplier = existing;
                }
            }
            return supplier.get();
        }
    }

    private interface Supplier<T>
    {
        T get();
    }

    private static class DefaultSupplier<T> implements Supplier<T>
    {
        private final ConfigResolver.TypedResolver<T> delegate;

        private DefaultSupplier(final ConfigResolver.TypedResolver<T> delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public T get()
        {
            return delegate.getValue();
        }
    }
}
