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
package org.apache.deltaspike.test.servlet.impl.event.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import javax.inject.Inject;

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.servlet.impl.util.ArchiveUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Test which validates that CDI events are fired when sessions are created and destroyed
 */
@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class SessionEventsTest
{

    @Deployment
    public static WebArchive getDeployment()
    {
        return ShrinkWrap.create(WebArchive.class, SessionEventsTest.class.getSimpleName() + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndServletModuleArchive())
                .addClass(SessionEventsObserver.class)
                .addClass(CreateSessionServlet.class)
                .addClass(DestroySessionServlet.class)
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml")
                .addAsWebResource(new StringAsset("foobar"), "foobar.txt")
                .setWebXML(new StringAsset(
                        Descriptors.create(WebAppDescriptor.class)
                                .servlet(CreateSessionServlet.class, "/create-session")
                                .servlet(DestroySessionServlet.class, "/destroy-session")
                                .exportAsString()));

    }

    /**
     * Needs to be static so that the client can be reused between test methods
     */
    private static DefaultHttpClient client;

    @BeforeClass
    public static void initHttpClient()
    {
        client = new DefaultHttpClient();
        client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
    }

    @AfterClass
    public static void destroyHttpClient()
    {
        client = null;
    }

    @Inject
    private SessionEventsObserver observer;

    /**
     * First send a request to a standard resource which won't create a user session
     */
    @Test
    @RunAsClient
    @InSequence(1)
    public void sendStatelessRequest(@ArquillianResource URL contextPath) throws Exception
    {
        String url = new URL(contextPath, "foobar.txt").toString();
        HttpResponse response = client.execute(new HttpGet(url));
        assertEquals(200, response.getStatusLine().getStatusCode());
        EntityUtils.consumeQuietly(response.getEntity());
    }

    /**
     * The observer didn't any events because no session was created
     */
    @Test
    @InSequence(2)
    public void statelessRequestDoesntEmitEvents()
    {
        assertEquals(0, observer.getEventCount());
    }

    /**
     * Now send a request which creates a session
     */
    @Test
    @RunAsClient
    @InSequence(3)
    public void sendRequestToCreateSession(@ArquillianResource URL contextPath) throws Exception
    {
        String url = new URL(contextPath, "create-session").toString();
        HttpResponse response = client.execute(new HttpGet(url));
        assertEquals(200, response.getStatusLine().getStatusCode());
        EntityUtils.consumeQuietly(response.getEntity());
    }

    /**
     * The observer should have received a event
     */
    @Test
    @InSequence(4)
    public void shouldReceiveInitializedSessionEvent()
    {
        assertEquals(1, observer.getEventCount());
        assertTrue("Didn't receive expected event",
                observer.getEventLog().contains("Initialized HttpSession"));
    }

    /**
     * Now send a request which creates a session
     */
    @Test
    @RunAsClient
    @InSequence(5)
    public void sendRequestToDestroySession(@ArquillianResource URL contextPath) throws Exception
    {
        String url = new URL(contextPath, "destroy-session").toString();
        HttpResponse response = client.execute(new HttpGet(url));
        assertEquals(200, response.getStatusLine().getStatusCode());
        EntityUtils.consumeQuietly(response.getEntity());
    }

    /**
     * The observer should have received a event
     */
    @Test
    @InSequence(6)
    public void shouldReceiveDestroyedSessionEvent()
    {
        assertEquals(2, observer.getEventCount());
        assertTrue("Didn't receive expected event",
                observer.getEventLog().contains("Destroyed HttpSession"));
    }

}
