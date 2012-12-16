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
package org.apache.deltaspike.test.api.jmx;

import org.apache.deltaspike.core.api.jmx.MBeanExtension;
import org.apache.deltaspike.core.api.jmx.annotation.Description;
import org.apache.deltaspike.core.api.jmx.annotation.MBean;
import org.apache.deltaspike.core.api.jmx.annotation.ManagedAttribute;
import org.apache.deltaspike.core.api.jmx.annotation.ManagedOperation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class SimpleRegistrationTest {
    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class)
                    .addClass(MyMBean.class)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsLibraries(ShrinkWrap.create(JavaArchive.class)
                            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                            .addAsServiceProvider(Extension.class, MBeanExtension.class)
                            .addPackages(true, MBeanExtension.class.getPackage()));

    }

    @Inject
    private MyMBean myMBean;

    @Test
    public void checkMBean() throws Exception {
        assertEquals(0, myMBean.getCounter());
        myMBean.resetTo(2);

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final ObjectName on = new ObjectName("deltaspike:type=MBeans,name=" + MyMBean.class.getName());
        assertTrue(server.isRegistered(on));

        assertEquals(2, server.getAttribute(on, "counter"));
        assertEquals(6, server.invoke(on, "multiply", new Object[]{ 3 }, new String[0]));

        myMBean.resetTo(5);

        assertEquals(5, server.getAttribute(on, "counter"));
        assertEquals(20, server.invoke(on, "multiply", new Object[]{ 4 }, new String[0]));

        server.setAttribute(on, new Attribute("counter", 10));
        assertEquals(10, myMBean.getCounter());
    }

    @MBean
    @Description("my mbean")
    @ApplicationScoped
    public static class MyMBean
    {
        private int counter = 0;

        @ManagedAttribute
        @Description("get counter")
        public int getCounter()
        {
            return counter;
        }

        @ManagedAttribute
        @Description("set counter")
        public void setCounter(final int counter)
        {
            this.counter = counter;
        }

        public void resetTo(final int value)
        {
            counter = value;
        }

        @ManagedOperation
        @Description("multiply counter")
        public int multiply(final int n)
        {
            return counter * n;
        }
    }
}
