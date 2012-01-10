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

package org.apache.deltaspike.integration.core.api.provider;


import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.integration.FullProfileCategory;
import org.apache.deltaspike.integration.SeCategory;
import org.apache.deltaspike.integration.WebProfileCategory;
import org.apache.deltaspike.integration.util.ShrinkWrapArchiveUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.BeanManager;

@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class BeanManagerProviderTest
{
    /**
     * X TODO creating a WebArchive is only a workaround because JavaArchive cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {
        return ShrinkWrap.create(WebArchive.class)
                         .addAsLibraries(ShrinkWrapArchiveUtil.getArchives(null,
                                 "META-INF/beans.xml",
                                 new String[]{"org.apache.deltaspike.core"},
                                 null))
                .addClass(TestBean.class)
                        // Must add the categories to the archive
                .addClasses(SeCategory.class, WebProfileCategory.class, FullProfileCategory.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testBeanManagerProvider() throws Exception
    {
        BeanManagerProvider bmp = BeanManagerProvider.getInstance();
        Assert.assertNotNull(bmp);

        BeanManager bm = bmp.getBeanManager();
        Assert.assertNotNull(bm);
    }
}
