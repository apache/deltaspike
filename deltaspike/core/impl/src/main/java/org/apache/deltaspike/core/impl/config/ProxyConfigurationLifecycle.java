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
import java.lang.reflect.Proxy;
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
                new ConfigurationHandler(cacheFor <= 0 ? -1 : configuration.cacheUnit().toMillis(cacheFor)));
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

        private final ConcurrentMap<Method, ConfigResolver.TypedResolver<?>> resolvers =
                new ConcurrentHashMap<Method, ConfigResolver.TypedResolver<?>>();
        private final long cacheMs;

        private ConfigurationHandler(final long cacheMs)
        {
            this.cacheMs = cacheMs;
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

            ConfigResolver.TypedResolver<?> typedResolver = resolvers.get(method);
            if (typedResolver == null)
            {
                final ConfigProperty annotation = method.getAnnotation(ConfigProperty.class);
                if (annotation == null)
                {
                    throw new UnsupportedOperationException(
                            method + " doesn't have @ConfigProperty and therefore is illegal");
                }

                // handle primitive bridge there (cdi doesnt support primitives but no reason our proxies don't)
                Class<?> returnType = method.getReturnType();
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

                typedResolver = delegate.asResolver(
                        annotation.name(), annotation.defaultValue(), returnType,
                        annotation.converter(), annotation.parameterizedBy(),
                        annotation.projectStageAware(), annotation.evaluateVariables());
                if (cacheMs > 0)
                {
                    typedResolver.cacheFor(MILLISECONDS, cacheMs);
                }

                final ConfigResolver.TypedResolver<?> existing = resolvers.putIfAbsent(method, typedResolver);
                if (existing != null)
                {
                    typedResolver = existing;
                }
            }
            return typedResolver.getValue();
        }
    }
}
