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

import static java.lang.Thread.currentThread;
import static java.lang.reflect.Proxy.newProxyInstance;
import static org.apache.deltaspike.data.test.util.TestDeployments.initDeployment;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class QueryStringExtractorFactoryTest
{

    private static final String QUERY_STRING = "it works";

    @Inject
    private QueryStringExtractorFactory factory;

    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment();
    }

    @Test
    public void should_unwrap_query_even_proxied()
    {
        // when
        String extracted = factory.extract((Query) newProxyInstance(currentThread().getContextClassLoader(),
                        new Class[] { Query.class }, new InvocationHandler()
            {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                {
                    if (method.getName().equals("toString"))
                    {
                        return "Unknown provider wrapper for tests.";
                    }
                    if (method.getName().equals("unwrap"))
                    {
                        Class<?> clazz = (Class<?>) args[0];
                        if (clazz.getName().contains("hibernate") ||
                                clazz.getName().contains("openjpa") ||
                                clazz.getName().contains("eclipse"))
                        {
                            return createProxy(clazz);
                        }
                        else
                        {
                            throw new PersistenceException("Unable to unwrap for " + clazz);
                        }
                    }
                    return null;
                }
            })
        );

        // then
        assertEquals(QUERY_STRING, extracted);
    }

    private static Object createProxy(Class<?> forClass)
    {
        return Proxy.newProxyInstance(currentThread().getContextClassLoader(),
                new Class[] { forClass },
                new FakeQueryInvocationHandler());
    }

    private static Object createInstance(Method method) throws Exception
    {
        // EclipseLink specific
        Class<?> returnType = Class.forName("org.eclipse.persistence.queries.DataReadQuery");
        Object instance = returnType.newInstance();
        Method setter = returnType.getMethod("setJPQLString", String.class);
        setter.invoke(instance, QUERY_STRING);
        return instance;
    }

    private static class FakeQueryInvocationHandler implements InvocationHandler
    {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (!method.getReturnType().equals(String.class))
            {
                if (method.getReturnType().isInterface())
                {
                    return createProxy(method.getReturnType());
                }
                return createInstance(method);
            }
            return QUERY_STRING; // we don't care of the result actually
        }

    }

}
