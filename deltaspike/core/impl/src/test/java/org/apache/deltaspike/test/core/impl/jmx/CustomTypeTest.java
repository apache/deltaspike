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
package org.apache.deltaspike.test.core.impl.jmx;

import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class CustomTypeTest {
    @Deployment
    public static Archive<?> war()
    {
        return ShrinkWrap.create(WebArchive.class, "CustomTypeTest.war")
            .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addClasses(CustomType.class);
    }

    @Inject
    private CustomType myMBean;

    @Test
    public void checkMBean() throws Exception
    {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        myMBean.setCounter(0);
        assertEquals(0, myMBean.getCounter());
        myMBean.setCounter(2);
        final ObjectName on = new ObjectName("cat:type=and,name=fish");
        assertTrue(server.isRegistered(on));
        assertEquals(2, server.getAttribute(on, "counter"));
        myMBean.setCounter(5);
        assertEquals(5, server.getAttribute(on, "counter"));
    }
}
