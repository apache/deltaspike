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
package org.apache.deltaspike.test.core.api.partialbean.uc003;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.core.api.partialbean.shared.CustomInterceptorImpl;
import org.apache.deltaspike.test.core.api.partialbean.shared.TestPartialBeanBinding;
import org.apache.deltaspike.test.core.api.partialbean.util.ArchiveUtils;
import org.apache.deltaspike.test.utils.CdiContainerUnderTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
@Category(SeCategory.class) //TODO use different category (only new versions of weld)
public class PartialBeanAsAbstractClassWithInterceptorTest
{
    public static final String CONTAINER_WELD_2_0_0 = "weld-2\\.0\\.0\\..*";

    @Deployment
    public static WebArchive war()
    {
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
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndPartialBeanArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(beansXml, "beans.xml");
    }

    @Test
    @Ignore //TODO re-visit use-case (also see uc007)
    public void testPartialBeanAsAbstractClassWithInterceptor() throws Exception
    {
        // this test is known to not work under weld-2.0.0.Final and weld-2.0.0.SP1
        Assume.assumeTrue(!CdiContainerUnderTest.is(CONTAINER_WELD_2_0_0));

        // we only inject an Instance as the proxy creation for the Bean itself
        // would trigger a nasty bug in Weld-2.0.0
        PartialBean partialBean = BeanProvider.getContextualReference(PartialBean.class);
        Assert.assertNotNull(partialBean);

        String result = partialBean.getResult();

        Assert.assertEquals("partial-test-true", result);

        result = partialBean.getManualResult();

        Assert.assertEquals("manual-test-true", result);

        //TODO test pre-destroy callback
    }
}
