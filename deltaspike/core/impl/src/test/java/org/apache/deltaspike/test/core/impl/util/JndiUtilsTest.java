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
package org.apache.deltaspike.test.core.impl.util;

import static org.junit.Assert.assertNotNull;

import java.util.Map;

import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.impl.util.JndiUtils;
import org.apache.deltaspike.test.category.WebProfileCategory;
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

@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class JndiUtilsTest
{
    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "jndiTest.jar")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, "jndiUtils.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    /**
     * Tests {@link JndiUtils#lookup(String, Class)} by looking up the {@link BeanManager}
     */
    @Test
    public void testLookup()
    {
        BeanManager beanManager = JndiUtils.lookup("java:comp/BeanManager", BeanManager.class);
        assertNotNull("JNDI lookup failed", beanManager);
    }

    /**
     * Tests {@link JndiUtils#list(String, Class)} by digging in java:comp namespace
     */
    @Test
    public void testList()
    {
        Map<String, BeanManager> beanManager = JndiUtils.list("java:comp", BeanManager.class);
        assertNotNull("JNDI lookup failed", beanManager);
    }
}
