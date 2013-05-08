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
package org.apache.deltaspike.core.impl.message;


import java.io.Serializable;
import java.lang.reflect.Proxy;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ReflectionUtils;

/**
 * The <code>TypedMessageBundleProducer</code> provides a producer method for
 * injected typed message bundles.
 */
public class TypedMessageBundleProducer implements Serializable
{
    private static final long serialVersionUID = -5077306523543940760L;

    @Produces
    @Dependent
    @TypedMessageBundle
    @SuppressWarnings("UnusedDeclaration")
    Object produceTypedMessageBundle(InjectionPoint injectionPoint, MessageBundleInvocationHandler handler)
    {
        return createMessageBundleProxy(ReflectionUtils.getRawType(injectionPoint.getType()), handler);
    }

    private <T> T createMessageBundleProxy(Class<T> type, MessageBundleInvocationHandler handler)
    {
        return type.cast(Proxy.newProxyInstance(ClassUtils.getClassLoader(null),
                new Class<?>[]{type}, handler));
    }
}
