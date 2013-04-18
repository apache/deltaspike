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
package org.apache.deltaspike.test.core.impl.scope.window;

import javax.inject.Inject;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.Assert;

@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class DefaultWindowContextTest
{
    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "defaultWindowContextTest.jar")
                .addPackage(DefaultWindowContextTest.class.getPackage().getName())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, "defaultWindowContextTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }


    @Inject
    private WindowContext windowContext;

    @Inject
    private SomeWindowScopedBean someWindowScopedBean;

    /**
     * Tests {@link org.apache.deltaspike.core.impl.util.JndiUtils#lookup(String, Class)} by looking up the {@link javax.enterprise.inject.spi.BeanManager}
     */
    @Test
    public void testWindowScoedBean()
    {
        Assert.assertNotNull(windowContext);
        Assert.assertNotNull(someWindowScopedBean);

        {
            windowContext.activateWindow("window1");
            someWindowScopedBean.setValue("Hans");
            Assert.assertEquals("Hans", someWindowScopedBean.getValue());
        }

        // now we switch it away to another 'window'
        {
            windowContext.activateWindow("window2");
            Assert.assertNull(someWindowScopedBean.getValue());
            someWindowScopedBean.setValue("Karl");
            Assert.assertEquals("Karl", someWindowScopedBean.getValue());
        }

        // and now back to the first window
        {
            windowContext.activateWindow("window1");

            // which must still contain the old value
            Assert.assertEquals("Hans", someWindowScopedBean.getValue());
        }

        // and again back to the second window
        {
            windowContext.activateWindow("window2");

            // which must still contain the old value of the 2nd window
            Assert.assertEquals("Karl", someWindowScopedBean.getValue());
        }

    }

}
