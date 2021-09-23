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

import java.net.URL;

import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.servlet.impl.util.ArchiveUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
 * Verifies that request injection into filters work as expected.
 * 
 * @see https://issues.apache.org/jira/browse/DELTASPIKE-414
 */
@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class EarlyRequestInjectionTest
{

    @Deployment(testable = false)
    public static WebArchive getDeployment()
    {
        return ShrinkWrap.create(WebArchive.class, EarlyRequestInjectionTest.class.getSimpleName() + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndServletModuleArchive())
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml")
                .addClass(EarlyRequestInjectionFilter.class)
                .addAsWebResource(new StringAsset("foobar"), "foobar.txt")
                .setWebXML(new StringAsset(
                        Descriptors.create(WebAppDescriptor.class)
                                .filter(EarlyRequestInjectionFilter.class, "/*")
                                .exportAsString()));

    }

    @ArquillianResource
    private URL contextPath;

    @Test
    public void shouldInjectRequestIntoFilters() throws Exception
    {

        String url = new URL(contextPath, "foobar.txt").toString();
        HttpResponse response = new DefaultHttpClient().execute(new HttpGet(url));

        assertEquals(200, response.getStatusLine().getStatusCode());

        Header[] verificationHeader = response.getHeaders("X-DS-Request-Injected");
        assertEquals(1, verificationHeader.length);
        assertEquals("true", verificationHeader[0].getValue());

    }

}
