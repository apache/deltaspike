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
import org.apache.deltaspike.partialbean.spi.PartialBeanProvider;
import org.apache.deltaspike.test.core.api.partialbean.shared.CustomInterceptorImpl;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Ignore("1) currently fails on wildfly/jboss because interceptors doesn't work on partial beans"
        + "2) arquillian doesn't deploy the TestPartialBeanProvider if tested != *-build-managed profiles")
public class PartialBeanAsAbstractClassWithInterceptorTest
{
    public static final String CONTAINER_WELD_2_0_0 = "weld-2\\.0\\.0\\..*";

    @Deployment
    public static WebArchive war()
    {
        // test doesn't work correclty on Weld 2.0.0 because PostConstruct isn't called on the partial bean
        if (CdiContainerUnderTest.is(CONTAINER_WELD_2_0_0))
        {
            return ShrinkWrap.create(WebArchive.class, "empty.war");
        }

        Asset beansXml = new StringAsset(
            "<beans><interceptors><class>" +
                    CustomInterceptorImpl.class.getName() +
            "</class></interceptors></beans>"
        );

        String simpleName = PartialBeanAsAbstractClassWithInterceptorTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, archiveName + ".jar")
                .addPackage(PartialBeanAsAbstractClassWithInterceptorTest.class.getPackage())
                .addPackage(TestPartialBeanBinding.class.getPackage())
                .addAsServiceProvider(PartialBeanProvider.class, TestPartialBeanProvider.class)
                .addAsManifestResource(beansXml, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndPartialBeanArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(beansXml, "beans.xml");
    }

    @Test
    public void testPartialBeanAsAbstractClassWithInterceptor() throws Exception
    {
        Assume.assumeTrue(!CdiContainerUnderTest.is(CONTAINER_WELD_2_0_0));

        PartialBean partialBean = BeanProvider.getContextualReference(PartialBean.class);
        Assert.assertNotNull(partialBean);

        String result = partialBean.getResult();

        Assert.assertEquals("partial-test-true", result);

        result = partialBean.getManualResult();

        Assert.assertEquals("manual-test-true", result);

        //TODO test pre-destroy callback
    }
}
