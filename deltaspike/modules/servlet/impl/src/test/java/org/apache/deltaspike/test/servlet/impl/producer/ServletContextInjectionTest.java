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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.servlet.impl.util.ArchiveUtils;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Test which validates that the {@link ServletContext} can be injected.
 */
@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class ServletContextInjectionTest
{

    @Deployment
    public static WebArchive getDeployment()
    {
        return ShrinkWrap.create(WebArchive.class, ServletContextInjectionTest.class.getSimpleName() + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndServletModuleArchive())
                .addAsWebInfResource(new StringAsset("<beans/>"), "beans.xml")
                .setWebXML(new StringAsset(
                        Descriptors.create(WebAppDescriptor.class)
                                .displayName("ServletContextInjection")
                                .exportAsString()));

    }

    @Inject
    @DeltaSpike
    private ServletContext servletContext;

    @Test
    public void shouldInjectServletContext() throws Exception
    {
        assertNotNull("ServletContext was not injected", servletContext);
        assertThat(servletContext.getServletContextName(), Matchers.is("ServletContextInjection"));
    }

}
