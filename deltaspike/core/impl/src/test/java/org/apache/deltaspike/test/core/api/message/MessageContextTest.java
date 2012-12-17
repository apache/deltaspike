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

import org.apache.deltaspike.core.api.message.LocaleResolver;
import org.apache.deltaspike.core.api.message.Message;
import org.apache.deltaspike.core.api.message.MessageContext;
import org.apache.deltaspike.core.impl.message.MessageBundleExtension;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.category.Serializer;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.junit.Assert;

/**
 * Tests for {@link MessageContext}
 */
@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class MessageContextTest
{
    @Inject
    private SimpleMessage simpleMessage;

    @Inject
    private MessageContext messageContext;

    /**
     * X TODO creating a WebArchive is only a workaround because JavaArchive
     * cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap
                .create(JavaArchive.class, "messageContextTest.jar")
                .addPackage(MessageContextTest.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap
                .create(WebArchive.class, "messageContextTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class,
                        MessageBundleExtension.class);
    }

    @Test
    public void testSimpleMessage()
    {
        Assert.assertEquals("Welcome to DeltaSpike",
                simpleMessage.welcomeTo(messageContext, "DeltaSpike").toString());
    }

    /**
     * We test that a non-existing category will fallback on the default message
     */
    @Test
    public void testSimpleMessageCategory()
    {
        Assert.assertEquals("Welcome to DeltaSpike",
                simpleMessage.welcomeTo(messageContext, "DeltaSpike").toString("notexisting"));
    }

    @Test
    public void resolveTextTest()
    {
        LocaleResolver localeResolver = new FixedEnglishLocalResolver();

        String messageText = messageContext
                .localeResolver(localeResolver)
                .messageResolver(new TestMessageResolver())
                .message().template("{hello}").argument("hans").toString();

        Assert.assertEquals("test message to hans", messageText);
    }

    @Test
    public void resolveGermanMessageTextTest()
    {
        LocaleResolver localeResolver = new FixedGermanLocaleResolver();
        String messageText = messageContext
                .localeResolver(localeResolver)
                .messageResolver(new TestMessageResolver())
                .message().template("{hello}").argument("hans").toString();

        Assert.assertEquals("Test Nachricht an hans", messageText);
    }

    @Test
    public void testArbitraryMessageContextRendering()
    {
        LocaleResolver localeResolver = new FixedGermanLocaleResolver();
        Message message = messageContext
                .localeResolver(localeResolver)
                .messageResolver(new TestMessageResolver())
                .message().template("{hello}").argument("hans");
        Assert.assertEquals("Test Nachricht an hans", message.toString());

        MessageContext messageContext2 = messageContext.clone().localeResolver(new FixedEnglishLocalResolver());
        Assert.assertEquals("test message to hans", message.toString(messageContext2));
    }

    @Test
    public void createInvalidMessageTest()
    {
        String messageText = messageContext.message().template("{xyz123}").toString();
        Assert.assertEquals("???xyz123???", messageText);

        messageText = messageContext
                .messageSource("nonexistingbundle.properties")
                .message()
                .template("{xyz123}")
                .toString();
        Assert.assertEquals("???xyz123???", messageText);
    }

    @Test
    public void createInvalidMessageWithArgumentsTest()
    {
        String messageText = messageContext.message().template("{xyz123}").
                argument("123").argument("456").argument("789").toString();

        Assert.assertEquals("???xyz123??? [123, 456, 789]", messageText);
    }

    @Test
    public void testMessageEquals()
    {
        Message m1 = messageContext.message();
        Message m2 = messageContext.message();
        Message m3 = messageContext.messageResolver(new TestMessageResolver()).message();

        Assert.assertEquals(m1, m1);
        Assert.assertEquals(m1, m2);
        Assert.assertEquals(m1, m3);
        Assert.assertEquals(m3, m1);
        Assert.assertEquals(m2, m3);

        m1.template("dumdidum").argument("nonono");
        m2.template("dumdidum").argument("nonono");
        Assert.assertEquals(m1, m2);

        Assert.assertEquals(m1.hashCode(), m2.hashCode());

        m2.argument("toomuch");
        Assert.assertFalse(m1.equals(m2));
        Assert.assertFalse(m2.equals(m1));
    }

    /**
     * Added check for System Property org.apache.deltaspike.weld.pre_1.1.10=true
     * If this exists then we will skip this test as it fails on WELD version < 1.1.10.Final
     * See DELTASPIKE-260
     */
    @Test
    public void testSerialization()
    {
        Assume.assumeTrue(System.getProperty("org.apache.deltaspike.weld.pre_1.1.10") == null);
        Serializer<Message> messageSerializer = new Serializer<Message>();

        LocaleResolver localeResolver = new FixedGermanLocaleResolver();
        Message message = messageContext
                .localeResolver(localeResolver)
                .messageResolver(new TestMessageResolver())
                .message().template("{hello}").argument("hans");
        Assert.assertEquals("Test Nachricht an hans", message.toString());

        Message messageClone = messageSerializer.roundTrip(message);

        Assert.assertEquals(message, messageClone);
        Assert.assertEquals("Test Nachricht an hans", messageClone.toString());

    }
}
