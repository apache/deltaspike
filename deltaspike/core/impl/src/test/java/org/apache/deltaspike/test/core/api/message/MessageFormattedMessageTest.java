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

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.apache.deltaspike.core.impl.message.MessageBundleExtension;
import org.apache.deltaspike.core.impl.message.MessageFormatMessageInterpolator;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link MessageFormatMessageInterpolator}
 * formatted messages.
 */
@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class MessageFormattedMessageTest
{
    @Inject
    private MessageFormattedMessage message;


    /**
     * X TODO creating a WebArchive is only a workaround because JavaArchive
     * cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {
        Asset beansXml = new StringAsset(
                "<beans><alternatives>" +
                        "<class>" + MessageFormatMessageInterpolator.class.getName() + "</class>" +
                        "</alternatives></beans>"
        );
        JavaArchive testJar = ShrinkWrap
                .create(JavaArchive.class, "messageFormattedMessageTest.jar")
                .addPackage(MessageFormattedMessageTest.class.getPackage())
                .addAsManifestResource(beansXml, "beans.xml");

        return ShrinkWrap
                .create(WebArchive.class, "messageFormattedMessageTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class,
                        MessageBundleExtension.class);
    }

    @Test
    public void testSimpleMessage()
    {
        assertEquals("Welcome to DeltaSpike", message.welcomeTo("DeltaSpike"));
        assertEquals("The income since 42 days is 12.34", message.incomeSinceDays(42, 12.34f));
    }

    @Test
    public void testNullMessage()
    {
        assertEquals("Welcome to null", message.welcomeTo(null));
    }

    @Test
    public void testComplexMessageWithNull()
    {
        assertEquals("At null on null, project deltaspike had 10 commits.", message.commitsInProject(null, "deltaspike", 10));
    }
}
