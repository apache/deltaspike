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
package org.apache.deltaspike.test.core.impl.activation;

import org.apache.deltaspike.core.impl.util.ClassDeactivation;
import org.apache.deltaspike.test.core.api.provider.TestBean;
import org.apache.deltaspike.test.core.api.temptestutil.ShrinkWrapArchiveUtil;
import org.apache.deltaspike.test.util.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

/**
 * Test for {@link org.apache.deltaspike.core.api.activation.ClassDeactivator}
 */
@RunWith(Arquillian.class)
public class TestClassDeactivation
{
    /**
     *X TODO creating a WebArchive is only a workaround because JavaArchive cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {
        URL fileUrl = TestClassDeactivation.class.getClassLoader()
                .getResource("META-INF/apache-deltaspike.properties");

        return ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(ShrinkWrapArchiveUtil.getArchives(null,
                        "META-INF/beans.xml",
                        new String[]{"org.apache.deltaspike.test.core.impl.activation"},
                        null))
                .addClass(TestBean.class)
                .addAsResource(FileUtils.getFileForURL(fileUrl.toString()))
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    /**
     * Tests if a class of the added package is active
     */
    @Test
    public void testActivatedClass()
    {
        Assert.assertTrue(ClassDeactivation.isClassActivated(ActivatedClass.class));
    }

    /**
     * Tests if the class deactivated by {@link TestClassDeactivator} is recognized as such
     */
    @Test
    public void testDeactivatedClass()
    {
        Assert.assertFalse(ClassDeactivation.isClassActivated(DeactivatedClass.class));
    }
}
