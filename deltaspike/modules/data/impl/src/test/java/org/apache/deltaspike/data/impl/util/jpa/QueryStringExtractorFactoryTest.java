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
package org.apache.deltaspike.data.impl.util.jpa;

import org.junit.Assert;
import org.junit.Test;

import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class QueryStringExtractorFactoryTest
{

    @Test
    public void should_unwrap_query_even_proxied()
    {
        // given

        // when
        String extractor = new QueryStringExtractorFactory().extract((Query) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{Query.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("toString")) return "Unknown provider wrapper for tests.";
                if (method.getName().equals("unwrap")) {
                    Class<?> clazz = (Class<?>) args[0];
                    if (clazz.getName().contains("hibernate") || clazz.getName().contains("openjpa") || clazz.getName().contains("eclipse")) {
                        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{clazz}, new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                if (method.getName().equals("getQueryString")) return "it works";
                                return null;
                            }
                        }); // we don't care of teh result actually
                    } else {
                        throw new PersistenceException("Unable to unwrap for " + clazz);
                    }
                }
                return null;
            }
        }));

        // then
        Assert.assertEquals("it works", extractor);
    }

}
