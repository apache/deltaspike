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
package org.apache.deltaspike.test.core.api.partialbean.uc008;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.test.core.api.partialbean.shared.TestPartialBeanBinding;
import org.apache.deltaspike.test.core.api.partialbean.util.ArchiveUtils;
import org.apache.deltaspike.test.utils.CdiContainerUnderTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class PartialBeanTest
{
    private static final String CONTAINER_OWB_1_2_x_BEFORE_1_2_8 = "owb-1\\.2\\.[0-7]";
    private static final String CONTAINER_TOMEE_1_7_x = "tomee-1\\.7\\..*";
    
    @Deployment
    public static WebArchive war()
    {
        if (CdiContainerUnderTest.is(CONTAINER_OWB_1_2_x_BEFORE_1_2_8)
                || CdiContainerUnderTest.is(CONTAINER_TOMEE_1_7_x))
        {
            return ShrinkWrap.create(WebArchive.class, "empty.war");
        }
        
        String simpleName = PartialBeanTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, archiveName + ".jar")
                .addPackage(PartialBeanTest.class.getPackage())
                .addPackage(TestPartialBeanBinding.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        WebArchive webArchive =  ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndPartialBeanArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return webArchive;
    }

    @Test
    public void testPartialBeanWithApplicationScope() throws Exception
    {
        // this test is known to not work under OWB 1.2.0 till 1.2.7 - see OWB #1036
        Assume.assumeTrue(!CdiContainerUnderTest.is(CONTAINER_OWB_1_2_x_BEFORE_1_2_8)
                && !CdiContainerUnderTest.is(CONTAINER_TOMEE_1_7_x));
        
        PartialBean bean = BeanProvider.getContextualReference(PartialBean.class);
        bean.test(this);            
    }
}
