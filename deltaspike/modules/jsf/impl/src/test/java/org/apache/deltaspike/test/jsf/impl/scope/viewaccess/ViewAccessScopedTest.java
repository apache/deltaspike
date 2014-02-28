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
package org.apache.deltaspike.test.jsf.impl.scope.viewaccess;

import javax.inject.Inject;
import org.apache.deltaspike.core.impl.scope.DeltaSpikeContextExtension;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.jsf.impl.scope.viewaccess.beans.ViewAccessScopedBeanX;
import org.apache.deltaspike.test.jsf.impl.scope.viewaccess.beans.ViewAccessScopedBeanY;
import org.apache.deltaspike.test.jsf.impl.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class ViewAccessScopedTest
{
    @Deployment
    public static WebArchive deploy()
    {
        String simpleName = ViewAccessScopedTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, archiveName + ".jar")
                .addPackage(ViewAccessScopedTest.class.getPackage().getName())
                .addPackage(ViewAccessScopedBeanX.class.getPackage().getName())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJsfArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private WindowContext windowContext;

    @Inject
    private ViewAccessScopedBeanX viewAccessScopedBeanX;

    @Inject
    private ViewAccessScopedBeanY viewAccessScopedBeanY;

    @Inject
    private DeltaSpikeContextExtension contextExtension;

    @Test
    public void usageOnOnePageTest()
    {
        windowContext.activateWindow("w1");

        viewAccessScopedBeanX.setValue("x1");
        viewAccessScopedBeanY.setValue("y1");
        Assert.assertEquals("x1", viewAccessScopedBeanX.getValue());
        Assert.assertEquals("y1", viewAccessScopedBeanY.getValue());
        contextExtension.getViewAccessScopedContext().onRenderingFinished("viewA");
        Assert.assertEquals("x1", viewAccessScopedBeanX.getValue());
        Assert.assertEquals("y1", viewAccessScopedBeanY.getValue());
        contextExtension.getViewAccessScopedContext().onRenderingFinished("viewA");

        //no access
        contextExtension.getViewAccessScopedContext().onRenderingFinished("viewB");

        //fails:
        Assert.assertNull(viewAccessScopedBeanX.getValue());
        Assert.assertNull(viewAccessScopedBeanY.getValue());
    }
}