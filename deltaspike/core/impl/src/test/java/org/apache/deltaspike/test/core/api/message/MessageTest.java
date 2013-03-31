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

import org.apache.deltaspike.core.impl.message.MessageBundleExtension;
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

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link org.apache.deltaspike.core.api.message.MessageTemplate}
 */
@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class MessageTest
{
    @Inject
    private TestMessages messages;

    /**
     * X TODO creating a WebArchive is only a workaround because JavaArchive
     * cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap
                .create(JavaArchive.class, "messageTest.jar")
                .addPackage(MessageTest.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap
                .create(WebArchive.class, "messageTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class,
                        MessageBundleExtension.class);
    }

    @Test
    public void testMessageBundleInjection(Jay jay)
    {
        assertEquals(TestMessageInterpolator.SPECIALFORMATTED + "Spotted 8 jays", jay.getMessage());
    }

    @Test
    public void testInternationalizedMessage()
    {
        assertEquals(TestMessageInterpolator.SPECIALFORMATTED + "Welcome to DeltaSpike", messages.welcomeToDeltaSpike());
    }

    @Test
    public void testInternationalizedParametrizedMessage()
    {
        assertEquals(TestMessageInterpolator.SPECIALFORMATTED + "Welcome to Apache DeltaSpike", messages.welcomeTo("Apache DeltaSpike"));
    }

    @Test
    public void testMessageCategory()
    {
        assertEquals(TestMessageInterpolator.SPECIALFORMATTED + "Value good was set"
            , messages.messageWithCategory("good").toString());

        assertEquals(TestMessageInterpolator.SPECIALFORMATTED + "The value of the property has been set to good."
            , messages.messageWithCategory("good").toString("longText"));

        assertEquals(TestMessageInterpolator.SPECIALFORMATTED + "Value good was set"
                , messages.messageWithCategory("good").toString("notExistingCategory"));
    }


}
