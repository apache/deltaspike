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

import org.junit.Test;

import javax.inject.Inject;
import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class SimpleRegistrationTest {
    private static MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    @Inject
    private MyMBean myMBean;

    @Test
    public void checkMBean() throws Exception {
        assertEquals(0, myMBean.getCounter());
        myMBean.resetTo(2);
        final ObjectName on = new ObjectName("org.apache.deltaspike:type=MBeans,name=" + MyMBean.class.getName());
        assertTrue(server.isRegistered(on));

        assertEquals(2, server.getAttribute(on, "counter"));
        assertEquals(6, server.invoke(on, "multiply", new Object[]{3}, new String[0]));

        myMBean.resetTo(5);

        assertEquals(5, server.getAttribute(on, "counter"));
        assertEquals(20, server.invoke(on, "multiply", new Object[]{4}, new String[0]));

        server.setAttribute(on, new Attribute("counter", 10));
        assertEquals(10, myMBean.getCounter());

        final Collection<Notification> notifications = new ArrayList<Notification>();
        server.addNotificationListener(on, new NotificationListener() {
            @Override
            public void handleNotification(final Notification notification, final Object handback) {
                notifications.add(notification);
            }
        }, null, null);
        myMBean.broadcast();
        assertEquals(1, notifications.size());
        assertEquals(10L, notifications.iterator().next().getSequenceNumber());
    }
}
