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

import org.apache.deltaspike.core.impl.util.JndiUtils;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.apache.deltaspike.test.core.api.temptestutil.ShrinkWrapArchiveUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.BeanManager;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
@Category(WebProfileCategory.class)
public class JndiUtilsTest
{

    @Deployment
    public static WebArchive deploy()
    {
        return ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(ShrinkWrapArchiveUtil.getArchives(null,
                        "META-INF/beans.xml",
                        new String[] { "org.apache.deltaspike" },
                        null))
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

}
