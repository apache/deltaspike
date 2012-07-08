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
package org.apache.deltaspike.test.core.api.message.broken;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.apache.deltaspike.core.impl.message.MessageBundleExtension;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
//X import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.experimental.categories.Category;
//X import org.junit.Test;
//X import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Tests for broken MessageBundle definition
 *
 * This is currently disabled due to a few problems with DeploymentException validation:
 * a.) some containers wrap the internally thrown IllegalStateException into another Exception
 * b.) some arquillian container adapters throw up with a NPE
 *
 * TODO: fix the container and arquillian setup!
 */
//X @RunWith(Arquillian.class)
@Category(SeCategory.class)
public class BrokenMessageBundleOnClassTest
{
    @Inject
    private BrokenMessageBundleClass messageBundle;

    /**
     * X TODO creating a WebArchive is only a workaround because JavaArchive
     * cannot contain other archives.
     */
    @Deployment
    @ShouldThrowException(Exception.class)
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap
                .create(JavaArchive.class, "invalidMessageBundleTest.jar")
                .addPackage(BrokenMessageBundleClass.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap
                .create(WebArchive.class, "invalidMessageBundleTest.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class,
                        MessageBundleExtension.class);
    }

    //X     @Test
    public void testSimpleMessage()
    {
        assertEquals("Welcome to DeltaSpike", messageBundle.welcomeToDeltaSpike());
    }
}
