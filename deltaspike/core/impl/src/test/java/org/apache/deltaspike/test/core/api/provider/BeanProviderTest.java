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
package org.apache.deltaspike.test.core.api.provider;


import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.test.core.api.temptestutil.ShrinkWrapArchiveUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(Arquillian.class)
public class BeanProviderTest
{
    /**
     *X TODO creating a WebArchive is only a workaround because JavaArchive cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {
        new BeanManagerProvider() {
            @Override
            public void setTestMode()
            {
                super.setTestMode();
            }
        }.setTestMode();

        return ShrinkWrap.create(WebArchive.class, "beanProvider.war")
                .addAsLibraries(ShrinkWrapArchiveUtil.getArchives(null,
                          "META-INF/beans.xml",
                          new String[]{"org.apache.deltaspike.core",
                                       "org.apache.deltaspike.test.category",
                                       "org.apache.deltaspike.test.core.api.provider"},
                          null))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    /**
     * lookup by type
     */
    @Test
    public void simpleBeanLookupByType()
    {
        TestBean testBean = BeanProvider.getContextualReference(TestBean.class, false);

        Assert.assertNotNull(testBean);
    }

    /**
     * lookup by name with expected type
     */
    @Test
    public void simpleBeanLookupByName()
    {
        TestBean testBean = BeanProvider.getContextualReference("extraNameBean", false, TestBean.class);

        Assert.assertNotNull(testBean);
    }

    /**
     * lookup by name without type
     */
    @Test
    public void simpleBeanLookupByNameWithoutType()
    {
        Object testBean = BeanProvider.getContextualReference("extraNameBean", false);

        Assert.assertNotNull(testBean);
    }

    /*
     * lookup without result
     */
    @Test
    public void optionalBeanLookup()
    {
        NoBean result = BeanProvider.getContextualReference(NoBean.class, true);

        Assert.assertNull(result);
    }

    /*
     * lookup of all beans of a given type
     */
    @Test
    public void multiBeanLookupWithDependentBean() throws Exception
    {
        List<MultiBean> result = BeanProvider.getContextualReferences(MultiBean.class, false);

        Assert.assertNotNull(result);

        Assert.assertEquals(2, result.size());
    }

    /*
     * lookup of all beans of a given type which aren't dependent scoped
     */
    @Test
    public void multiBeanLookupWithoutDependentBean() throws Exception
    {
        List<MultiBean> result = BeanProvider.getContextualReferences(MultiBean.class, false, false);

        Assert.assertNotNull(result);

        Assert.assertEquals(1, result.size());
    }
}
