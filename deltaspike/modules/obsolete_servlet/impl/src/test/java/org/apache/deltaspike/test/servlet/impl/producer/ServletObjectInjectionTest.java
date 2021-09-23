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
package org.apache.deltaspike.test.servlet.impl.producer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.URL;

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.servlet.impl.util.ArchiveUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Test which validates that servlet objects are correctly injected.
 */
@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class ServletObjectInjectionTest
{

    @Deployment(testable = false)
    public static WebArchive getDeployment()
    {
        return ShrinkWrap.create(WebArchive.class, ServletObjectInjectionTest.class.getSimpleName() + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndServletModuleArchive())
                .addClass(ServletObjectInjectionBean.class)
                .addClass(ServletObjectInjectionServlet.class)
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml")
                .setWebXML(new StringAsset(
                        Descriptors.create(WebAppDescriptor.class)
                                .servlet(ServletObjectInjectionServlet.class, "/servlet-object-injecetion")
                                .exportAsString()));

    }

    @ArquillianResource
    private URL contextPath;

    @Test
    public void shouldInjectServletRequest() throws Exception
    {
        assertThat(responseFromServlet(), Matchers.containsString("[ServletRequest=OK]"));
    }

    @Test
    public void shouldInjectHttpServletRequest() throws Exception
    {
        assertThat(responseFromServlet(), Matchers.containsString("[HttpServletRequest=OK]"));
    }

    @Test
    public void shouldInjectServletResponse() throws Exception
    {
        assertThat(responseFromServlet(), Matchers.containsString("[ServletResponse=OK]"));
    }

    @Test
    public void shouldInjectHttpServletResponse() throws Exception
    {
        assertThat(responseFromServlet(), Matchers.containsString("[HttpServletResponse=OK]"));
    }

    @Test
    public void shouldInjectHttpSession() throws Exception
    {
        assertThat(responseFromServlet(), Matchers.containsString("[HttpSession=OK]"));
    }

    private String responseFromServlet() throws Exception
    {
        String url = new URL(contextPath, "servlet-object-injecetion").toString();
        HttpResponse response = new DefaultHttpClient().execute(new HttpGet(url));
        assertEquals(200, response.getStatusLine().getStatusCode());
        return EntityUtils.toString(response.getEntity());
    }

}
