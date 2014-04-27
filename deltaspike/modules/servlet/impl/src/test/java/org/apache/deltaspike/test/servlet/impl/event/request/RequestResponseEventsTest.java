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
package org.apache.deltaspike.test.servlet.impl.event.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import javax.inject.Inject;

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.servlet.impl.util.ArchiveUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Test which validates that CDI events are fired when requests and responses are created or destroyed
 */
@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class RequestResponseEventsTest
{

    @Deployment
    public static WebArchive getDeployment()
    {
        return ShrinkWrap.create(WebArchive.class, RequestResponseEventsTest.class.getSimpleName() + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndServletModuleArchive())
                .addClass(RequestResponseEventsObserver.class)
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml")
                .addAsWebResource(new StringAsset("foobar"), "foobar.txt");
    }

    @Inject
    private RequestResponseEventsObserver observer;

    @Test
    @RunAsClient
    @InSequence(2)
    public void sendRequest(@ArquillianResource URL contextPath) throws Exception
    {
        String url = new URL(contextPath, "foobar.txt").toString();
        HttpResponse response = new DefaultHttpClient().execute(new HttpGet(url));
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    @InSequence(3)
    public void shouldReceiveRequestInitializedEvent()
    {
        assertTrue("Didn't receive expected event",
                observer.getEventLog().contains("Initialized HttpServletRequest: /RequestResponseEventsTest/foobar.txt"));
    }

    @Test
    @InSequence(3)
    public void shouldReceiveResponseInitializedEvent()
    {
        assertTrue("Didn't receive expected event",
                observer.getEventLog().contains("Initialized HttpServletResponse"));
    }

    @Test
    @InSequence(3)
    public void shouldReceiveRequestDestroyedEvent()
    {
        assertTrue("Didn't receive expected event",
                observer.getEventLog().contains("Destroyed HttpServletRequest: /RequestResponseEventsTest/foobar.txt"));
    }

    @Test
    @InSequence(3)
    public void shouldReceiveResponseDestroyedEvent()
    {
        assertTrue("Didn't receive expected event",
                observer.getEventLog().contains("Destroyed HttpServletResponse"));
    }

}
