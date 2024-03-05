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
package org.apache.deltaspike.test.core.api.partialbean.uc007;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.test.core.api.partialbean.shared.CustomInterceptor;
import org.apache.deltaspike.test.core.api.partialbean.shared.CustomInterceptorImpl;
import org.apache.deltaspike.test.core.api.partialbean.shared.CustomInterceptorState;
import org.apache.deltaspike.test.core.api.partialbean.shared.TestPartialBeanBinding;
import org.apache.deltaspike.test.core.api.partialbean.util.ArchiveUtils;
import org.apache.deltaspike.test.utils.CdiContainerUnderTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MethodLevelInterceptorTest
{
    @Deployment
    public static WebArchive war()
    {
        Asset beansXml = new StringAsset(
            "<beans bean-discovery-mode=\"all\"><interceptors><class>" +
                    CustomInterceptorImpl.class.getName() +
            "</class></interceptors></beans>"
        );

        String simpleName = MethodLevelInterceptorTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, archiveName + ".jar")
                .addPackage(PartialBean.class.getPackage())
                .addPackage(CustomInterceptor.class.getPackage())
                .addAsManifestResource(beansXml, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndPartialBeanArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(beansXml, "beans.xml");
    }

    @Test
    public void testMethodLevelInterceptor() throws Exception
    {
        PartialBean partialBean = BeanProvider.getContextualReference(PartialBean.class);
        CustomInterceptorState state = BeanProvider.getContextualReference(CustomInterceptorState.class);

        Assert.assertNotNull(partialBean);
        partialBean.doSomething();
        Assert.assertEquals(true, state.isIntercepted());
    }
    
    @Test
    public void testMethodLevelInterceptorStereotype() throws Exception
    {
        PartialBean partialBean = BeanProvider.getContextualReference(PartialBean.class);
        CustomInterceptorState state = BeanProvider.getContextualReference(CustomInterceptorState.class);

        Assert.assertNotNull(partialBean);
        partialBean.doSomething2();
        Assert.assertEquals(true, state.isIntercepted());
    }

    @Test
    public void testMethodLevelInterceptorOnAbstractMethod() throws Exception
    {
        PartialBean partialBean = BeanProvider.getContextualReference(PartialBean.class);
        CustomInterceptorState state = BeanProvider.getContextualReference(CustomInterceptorState.class);

        Assert.assertNotNull(partialBean);
        Assert.assertEquals("partial", partialBean.getResult());
        Assert.assertEquals(true, state.isIntercepted());
    }
}
