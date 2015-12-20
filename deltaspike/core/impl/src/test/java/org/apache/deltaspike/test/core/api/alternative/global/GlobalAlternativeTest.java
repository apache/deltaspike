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
package org.apache.deltaspike.test.core.api.alternative.global;


import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.core.api.alternative.global.qualifier.AlternativeBaseBeanB;
import org.apache.deltaspike.test.core.api.alternative.global.qualifier.BaseBeanA;
import org.apache.deltaspike.test.core.api.alternative.global.qualifier.BaseInterface;
import org.apache.deltaspike.test.core.api.alternative.global.qualifier.QualifierA;
import org.apache.deltaspike.test.core.api.alternative.global.qualifier.QualifierB;
import org.apache.deltaspike.test.core.api.alternative.global.qualifier.QualifierValue1;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * Tests for @Alternative across BDAs
 */
@RunWith(Arquillian.class)
//X TODO remove the restriction to SeCategory after fixing the packaging issue
@Category(SeCategory.class)
public class GlobalAlternativeTest
{
    @Inject
    private BaseInterface1 bean;

    @Inject
    @QualifierA(QualifierValue1.class)
    private BaseInterface beanA;

    @Inject
    @QualifierB(QualifierValue1.class)
    private BaseInterface beanB;

    /**
     * X TODO creating a WebArchive is only a workaround because JavaArchive cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {

        Asset beansXml = EmptyAsset.INSTANCE;

        try
        {
            // if this doesn't throw a ClassNotFoundException, then we have OpenWebBeans on the ClassPath.
            Class.forName("org.apache.webbeans.spi.ContainerLifecycle");

            // Older OWB versions had an error with @Alternatives pickup from AnnotatedType.
            // But as OWB has global-alternatives behaviour by default, we can simply add them
            // via beans.xml. From OWB-1.2.1 on we would not need this anymore, but it's still needed
            // for 1.0.x, 1.1.x and 1.2.0 series
            beansXml = new StringAsset(
                    "<beans><alternatives>" +
                            "<class>" + BaseInterface1AlternativeImplementation.class.getName() + "</class>" +
                            "<class>" + SubBaseBean2.class.getName() + "</class>" +
                            "<class>" + AlternativeBaseBeanB.class.getName() + "</class>" +
                            "</alternatives></beans>"
            );
        }
        catch (ClassNotFoundException cnfe)
        {
            // all fine, no OWB here -> Weld handling
        }


        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "excludeIntegrationTest.jar")
                .addPackage(GlobalAlternativeTest.class.getPackage())
                .addPackage(QualifierA.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, "globalAlternative.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive(new String[]{"META-INF.config"}))
                .addAsLibraries(testJar)
                .addAsWebInfResource(beansXml, "beans.xml");
    }

    /**
     * The alternative configured in the low-level config should get used instead of the default implementation
     */
    @Test
    public void alternativeImplementationWithClassAsBaseType()
    {
        BaseBean1 testBean = BeanProvider.getContextualReference(BaseBean1.class);

        Assert.assertEquals(SubBaseBean2.class.getName(), testBean.getClass().getName());
    }

    /**
     * The alternative configured in the low-level config should get used instead of the default implementation
     */
    @Test
    public void alternativeImplementationWithInterfaceAsBaseType()
    {
        Assert.assertEquals(BaseInterface1AlternativeImplementation.class.getName(), bean.getClass().getName());
    }

    @Test
    public void alternativeImplementationWithInterfaceAsBaseTypeAndQualifier()
    {
        Assert.assertEquals(BaseBeanA.class.getName(), beanA.getClass().getName());
        Assert.assertEquals(AlternativeBaseBeanB.class.getName(), beanB.getClass().getName());
    }
}
