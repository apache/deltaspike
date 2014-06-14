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
package org.apache.deltaspike.test.core.api.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import javax.inject.Inject;

import org.apache.deltaspike.core.util.ProxyUtils;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ProxyUtilsTest
{

    @Inject
    private MyBean myDependentScopedBean;

    @Inject
    private MyInterface myInterface;

    @Deployment
    public static Archive<?> createTestArchive()
    {
        return ShrinkWrap
                .create(WebArchive.class, "proxyUtil.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(ProxyUtilsTest.class, MyBean.class, MyInterface.class,
                        MyInterfaceImpl.class);
    }

    @Test
    public void testIsIntefaceProxy()
    {
        Object proxy = Proxy.newProxyInstance(myDependentScopedBean.getClass().getClassLoader(),
                new Class[] { MyInterface.class }, new InvocationHandler()
                {

                    @Override
                    public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable
                    {
                        return null;
                    }
                });
        Assert.assertTrue(ProxyUtils.isInterfaceProxy(proxy.getClass()));
    }

    @Test
    public void testIsNotIntefaceProxy()
    {
        Assert.assertFalse(ProxyUtils.isInterfaceProxy(myDependentScopedBean.getClass()));
    }

    @Test
    public void testIsProxiedClass()
    {
        Assert.assertTrue(ProxyUtils.isProxiedClass(myInterface.getClass()));
    }

    @Test
    public void testIsNotProxiedClass()
    {
        Assert.assertFalse(ProxyUtils.isProxiedClass(myDependentScopedBean.getClass()));
    }

    @Test
    public void testGetUnproxiedClass()
    {
        Class clazz = ProxyUtils.getUnproxiedClass(myInterface.getClass());
        Assert.assertEquals(clazz, MyInterfaceImpl.class);
    }

    @Test
    public void testGetProxyAndBaseTypes()
    {
        List<Class<?>> list = ProxyUtils.getProxyAndBaseTypes(myInterface.getClass());
        Assert.assertEquals(list.size(), 2);
        Assert.assertTrue(ProxyUtils.isProxiedClass(list.get(0)));
        Assert.assertEquals(MyInterfaceImpl.class, list.get(1));
    }

}
