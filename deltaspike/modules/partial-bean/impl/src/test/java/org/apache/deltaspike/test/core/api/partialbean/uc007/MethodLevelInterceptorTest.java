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
import org.apache.deltaspike.core.util.ClassUtils;
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
    public static final String CONTAINER_WELD_5_0 = "weld-5\\.0\\..*";

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

        //don't create a completely empty web-archive
        if (CdiContainerUnderTest.is(CONTAINER_WELD_5_0))
        {
            return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                    .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndPartialBeanArchive());
        }

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, archiveName + ".jar")
                .addPackage(MethodLevelInterceptorTest.class.getPackage())
                .addPackage(TestPartialBeanBinding.class.getPackage())
                .addAsManifestResource(beansXml, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndPartialBeanArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(beansXml, "beans.xml");
    }

    @Test
    public void testMethodLevelInterceptor() throws Exception
    {
        // this test is known to not work under weld-5.0.x
        Assume.assumeTrue(!CdiContainerUnderTest.is(CONTAINER_WELD_5_0));

        // workaround as payara doesnt pass cdicontainer.version to the arquillian process
        if (ClassUtils.tryToLoadClassForName("org.apache.deltaspike.test.core.api.partialbean.uc007.PartialBean") == null)
        {
            return;
        }

        PartialBean partialBean = BeanProvider.getContextualReference(PartialBean.class);
        CustomInterceptorState state = BeanProvider.getContextualReference(CustomInterceptorState.class);

        Assert.assertNotNull(partialBean);
        partialBean.doSomething();
        Assert.assertEquals(true, state.isIntercepted());
    }
    
    @Test
    public void testMethodLevelInterceptorStereotype() throws Exception
    {
        // this test is known to not work under weld-5.0.x
        Assume.assumeTrue(!CdiContainerUnderTest.is(CONTAINER_WELD_5_0));

        // workaround as payara doesnt pass cdicontainer.version to the arquillian process
        if (ClassUtils.tryToLoadClassForName("org.apache.deltaspike.test.core.api.partialbean.uc007.PartialBean") == null)
        {
            return;
        }

        PartialBean partialBean = BeanProvider.getContextualReference(PartialBean.class);
        CustomInterceptorState state = BeanProvider.getContextualReference(CustomInterceptorState.class);

        Assert.assertNotNull(partialBean);
        partialBean.doSomething2();
        Assert.assertEquals(true, state.isIntercepted());
    }

    @Test
    public void testMethodLevelInterceptorOnAbstractMethod() throws Exception
    {
        // this test is known to not work under weld-5.0.x
        Assume.assumeTrue(!CdiContainerUnderTest.is(CONTAINER_WELD_5_0));

        // workaround as payara doesnt pass cdicontainer.version to the arquillian process
        if (ClassUtils.tryToLoadClassForName("org.apache.deltaspike.test.core.api.partialbean.uc007.PartialBean") == null)
        {
            return;
        }

        PartialBean partialBean = BeanProvider.getContextualReference(PartialBean.class);
        CustomInterceptorState state = BeanProvider.getContextualReference(CustomInterceptorState.class);

        Assert.assertNotNull(partialBean);
        Assert.assertEquals("partial", partialBean.getResult());
        Assert.assertEquals(true, state.isIntercepted());
    }
}
