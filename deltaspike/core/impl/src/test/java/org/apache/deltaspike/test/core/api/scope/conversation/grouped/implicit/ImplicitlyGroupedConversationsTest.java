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
package org.apache.deltaspike.test.core.api.scope.conversation.grouped.implicit;

import org.apache.deltaspike.core.spi.scope.conversation.GroupedConversationManager;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
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

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;

@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class ImplicitlyGroupedConversationsTest
{
    @Deployment
    public static WebArchive deploy()
    {
        String simpleName = ImplicitlyGroupedConversationsTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, archiveName + ".jar")
                .addPackage(ImplicitlyGroupedConversationsTest.class.getPackage().getName())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private WindowContext windowContext;

    @Inject
    private ImplicitlyGroupedBean implicitlyGroupedBean;

    @Inject
    private GroupedConversationManager conversationManager;

    @Test
    public void parallelConversationsTest()
    {
        windowContext.activateWindow("w1");

        implicitlyGroupedBean.setValue("x");
        Assert.assertEquals("x", implicitlyGroupedBean.getValue());

        windowContext.activateWindow("w2");

        Assert.assertNull(implicitlyGroupedBean.getValue());

        implicitlyGroupedBean.setValue("y");
        Assert.assertEquals("y", implicitlyGroupedBean.getValue());

        windowContext.activateWindow("w1");
        Assert.assertEquals("x", implicitlyGroupedBean.getValue());
    }

    @Test
    public void immediatelyClosedConversationTest()
    {
        windowContext.activateWindow("w1");

        implicitlyGroupedBean.setValue("x");
        Assert.assertEquals("x", implicitlyGroupedBean.getValue());

        implicitlyGroupedBean.done();

        Assert.assertNull(implicitlyGroupedBean.getValue());
    }

    @Test
    public void immediatelyClosedConversationViaConversationManagerTest()
    {
        windowContext.activateWindow("w1");

        implicitlyGroupedBean.setValue("x");
        Assert.assertEquals("x", implicitlyGroupedBean.getValue());

        this.conversationManager.closeConversation(ImplicitlyGroupedBean.class);

        Assert.assertNull(implicitlyGroupedBean.getValue());


        implicitlyGroupedBean.setValue("y");
        Assert.assertEquals("y", implicitlyGroupedBean.getValue());

        this.conversationManager.closeConversationGroup(ImplicitlyGroupedBean.class);

        Assert.assertNull(implicitlyGroupedBean.getValue());
    }

    @Test
    public void immediatelyClosedConversationsTest()
    {
        windowContext.activateWindow("w1");

        implicitlyGroupedBean.setValue("x");
        Assert.assertEquals("x", implicitlyGroupedBean.getValue());

        this.conversationManager.closeConversations();

        Assert.assertNull(implicitlyGroupedBean.getValue());
    }

    @Test
    public void immediatelyClosedConversationsViaWindowContextTest()
    {
        windowContext.activateWindow("w1");

        implicitlyGroupedBean.setValue("x");
        Assert.assertEquals("x", implicitlyGroupedBean.getValue());

        Assert.assertTrue(this.windowContext.closeWindow("w1"));
        windowContext.activateWindow("w1");

        Assert.assertNull(implicitlyGroupedBean.getValue());
    }

    @Test(expected = ContextNotActiveException.class)
    public void noWindowTest()
    {
        try
        {
            windowContext.activateWindow("w1");

            implicitlyGroupedBean.setValue("x");
            Assert.assertEquals("x", implicitlyGroupedBean.getValue());

            this.windowContext.closeWindow("w1");
        }
        catch (ContextNotActiveException e)
        {
            Assert.fail();
        }

        implicitlyGroupedBean.getValue();
    }
}
