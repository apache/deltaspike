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
package org.apache.deltaspike.test.core.api.scope.conversation.subgroup.uc002;

import org.apache.deltaspike.core.api.scope.ConversationGroup;
import org.apache.deltaspike.core.spi.scope.conversation.GroupedConversationManager;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.core.api.scope.conversation.subgroup.shared.TestBaseBean;
import org.apache.deltaspike.test.core.api.scope.conversation.subgroup.shared.TestBeanA;
import org.apache.deltaspike.test.core.api.scope.conversation.subgroup.shared.TestBeanB;
import org.apache.deltaspike.test.core.api.scope.conversation.subgroup.shared.TestBeanC;
import org.apache.deltaspike.test.core.api.scope.conversation.subgroup.shared.TestGroup;
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

import javax.inject.Inject;

@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class GroupedConversationSubGroupTest
{
    @Deployment
    public static WebArchive deploy()
    {
        String simpleName = GroupedConversationSubGroupTest.class.getSimpleName();
        String archiveName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, archiveName + ".jar")
                .addPackage(GroupedConversationSubGroupTest.class.getPackage())
                .addPackage(TestBaseBean.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, archiveName + ".war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private WindowContext windowContext;

    @Inject
    @ConversationGroup(TestGroup.class)
    private TestBeanA testBeanA;

    @Inject
    @ConversationGroup(TestGroup.class)
    private TestBeanB testBeanB;

    @Inject
    @ConversationGroup(TestGroup.class)
    private TestBeanC testBeanC;

    @Inject
    private GroupedConversationManager conversationManager;

    @Test
    public void closedSubGroupTest()
    {
        windowContext.activateWindow("w1");

        testBeanA.setValue("x1");
        testBeanB.setValue("x2");
        testBeanC.setValue("x3");
        Assert.assertEquals("x1", testBeanA.getValue());
        Assert.assertEquals("x2", testBeanB.getValue());
        Assert.assertEquals("x3", testBeanC.getValue());

        this.conversationManager.closeConversationGroup(TestSubGroup.class);

        Assert.assertNull(testBeanA.getValue());
        Assert.assertEquals("x2", testBeanB.getValue()); //not part of the sub-group
        Assert.assertNull(testBeanC.getValue());
    }
}
