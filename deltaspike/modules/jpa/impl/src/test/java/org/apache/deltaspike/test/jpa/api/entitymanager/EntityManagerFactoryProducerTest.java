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
package org.apache.deltaspike.test.jpa.api.entitymanager;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.spi.PersistenceProviderResolverHolder;

import org.apache.deltaspike.jpa.spi.entitymanager.PersistenceConfigurationProvider;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.jpa.api.shared.TestEntityManager;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class EntityManagerFactoryProducerTest
{

    @Deployment
    public static WebArchive deploy()
    {
        // set the dummy PersistenceProviderResolver which creates our DummyEntityManagerFactory
        PersistenceProviderResolverHolder.setPersistenceProviderResolver(new TestPersistenceProviderResolver());

        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "unitDefinitionTest.jar")
                .addPackage(ArchiveUtils.SHARED_PACKAGE)
                .addPackage(EntityManagerFactoryProducerTest.class.getPackage().getName())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(new StringAsset(TestPersistenceProviderResolver.class.getName()),
                        "META-INF/services/javax.persistence.spi.PersistenceProviderResolver");

        return ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJpaArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(ArchiveUtils.getBeansXml(), "beans.xml");
    }


    @Inject
    @SampleDb
    private EntityManager entityManager;

    private @Inject PersistenceConfigurationProvider persistenceConfigurationProvider;

    @Test
    public void testUnitDefinitionQualifier() throws Exception
    {
        Assert.assertNotNull(entityManager);
        Assert.assertNotNull(entityManager.getDelegate());

        Assert.assertTrue(entityManager.getDelegate() instanceof TestEntityManager);
        TestEntityManager tem = (TestEntityManager) entityManager.getDelegate();
        Assert.assertEquals("testPersistenceUnit", tem.getUnitName());
    }
}
