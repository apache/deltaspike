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
package org.apache.deltaspike.test.core.api.message;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.impl.message.MessageBundleExtension;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

/**
 * Tests for type-safe messages without {@link org.apache.deltaspike.core.api.message.MessageTemplate}
 */
@RunWith(Arquillian.class)
public class MinimalMessagesTest
{
    @Inject
    private MinimalMessages minimalMessages;

    @Inject
    private CustomMinimalMessages customMinimalMessages;

    @Inject
    private ElPickedUpMessages injectedElPickedUpMessages;

    /**
     * X TODO creating a WebArchive is only a workaround because JavaArchive
     * cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap
                .create(JavaArchive.class, "minimalMessageTest.jar")
                .addPackages(false, Filters.exclude(MessageContextTest.class),
                        MinimalMessagesTest.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap
                .create(WebArchive.class, "minimalMessageTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsResource("customMinimalMessage_en.properties")
                .addAsResource("org/apache/deltaspike/test/core/api/message/MinimalMessages_en.properties")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class,
                        MessageBundleExtension.class);
    }

    @Test
    public void testMinimalMessage()
    {
        Assert.assertEquals("Hello DeltaSpike", minimalMessages.sayHello("DeltaSpike"));
    }

    @Test
    public void testCustomMinimalMessage()
    {
        Assert.assertEquals("Hello DeltaSpike", customMinimalMessages.sayHello("DeltaSpike"));
    }

    @Test
    public void testExpressionLanguageIntegration()
    {
        ElPickedUpMessages elMessage =
                (ElPickedUpMessages) BeanProvider.getContextualReference("elPickedUpMessages");
        Assert.assertNotNull(elMessage);
        Assert.assertEquals("Hello DeltaSpike", elMessage.sayHello("DeltaSpike"));
        Assert.assertEquals("Hello null", elMessage.sayHello(null));
        Assert.assertEquals("Text", elMessage.text());
    }

    @Test
    public void testExpressionLanguageIntegrationWithCustomName()
    {
        TypedMessageWithCustomName elMessage =
                (TypedMessageWithCustomName) BeanProvider.getContextualReference("namedTypedMessages");
        Assert.assertNotNull(elMessage);
        Assert.assertEquals("Hello DeltaSpike", elMessage.sayHello("DeltaSpike"));
        Assert.assertEquals("Hello null", elMessage.sayHello(null));
        Assert.assertEquals("Text", elMessage.text());
    }

    @Test
    public void testInjectionOfNamedMessageBundle()
    {

        ElPickedUpMessages injectedElMessage = this.injectedElPickedUpMessages;
        Assert.assertNotNull(injectedElMessage);
        Assert.assertEquals("Hello DeltaSpike", injectedElMessage.sayHello("DeltaSpike"));
        Assert.assertEquals("Hello null", injectedElMessage.sayHello(null));
        Assert.assertEquals("Text", injectedElMessage.text());
    }
}
