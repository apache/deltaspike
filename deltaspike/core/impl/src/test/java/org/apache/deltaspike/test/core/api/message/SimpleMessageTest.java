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
import org.apache.deltaspike.core.impl.message.MessageBundleExtension;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.utils.Serializer;
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

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link org.apache.deltaspike.core.api.message.MessageTemplate}
 */
@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class SimpleMessageTest
{
    @Inject
    private SimpleMessage simpleMessage;

    @Inject
    private LocaleResolver localeResolver;

    @Inject
    private MessageUser messageUser;


    /**
     * X TODO creating a WebArchive is only a workaround because JavaArchive
     * cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap
                .create(JavaArchive.class, "simpleMessageTest.jar")
                .addPackage(SimpleMessageTest.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap
                .create(WebArchive.class, "simpleMessageTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class,
                        MessageBundleExtension.class);
    }

    @Test
    public void testSimpleMessage()
    {
        assertEquals("Welcome to DeltaSpike", simpleMessage.welcomeToDeltaSpike());
        assertEquals("Welcome to DeltaSpike", simpleMessage.welcomeWithStringVariable("DeltaSpike"));
    }

    /**
     * This test checks if the {@link org.apache.deltaspike.core.api.message.LocaleResolver}
     * gets properly invoked.
     */
    @Test
    public void testDefaultLocaleInMessage()
    {
        float f = 123.45f;

        String expectedResult = "Welcome to " + String.format(this.localeResolver.getLocale(), "%f", f);
        String result = simpleMessage.welcomeWithFloatVariable(f);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testNullMessage()
    {
        String expectedResult = "Welcome to null";
        String result = simpleMessage.welcomeWithStringVariable(null);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testPaddingWithNullMessage()
    {
        String expectedResult = "Counter: null";
        String result = simpleMessage.counter(null);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testMessageSerialisation()
    {
        Serializer<SimpleMessage> simpleMessageSerializer = new Serializer<SimpleMessage>();

        SimpleMessage sm2 = simpleMessageSerializer.roundTrip(simpleMessage);
        assertNotNull(sm2);
    }

    @Test
    public void testPassivationCapability()
    {
        assertEquals("Welcome to DeltaSpike", messageUser.getMsg().welcomeToDeltaSpike());
        assertEquals("Welcome to DeltaSpike", messageUser.getMsg().welcomeWithStringVariable("DeltaSpike"));
    }
}
