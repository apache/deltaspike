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

import java.lang.reflect.Proxy;

/**
 * A factory class to produce message bundle implementations.
 */
final class MessageFactory
{

    private MessageFactory()
    {
    }

    /**
     * Get a message bundle of the given type. Equivalent to
     * <code>{@link #getBundle(Class, java.util.Locale) getBundle}(type, Locale.getDefault())</code>
     * .
     * 
     * @param type
     *            the bundle type class
     * @param <T>
     *            the bundle type
     * @return the bundle
     */
    public static <T> T getBundle(Class<T> type)
    {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(),
                new Class<?>[]
                { type }, new MessageBundleInvocationHandler()));
    }

}
