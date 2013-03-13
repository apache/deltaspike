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
package org.apache.deltaspike.test.jpa.api.transactional.aggregation;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.jpa.impl.transaction.context.TransactionBeanStorage;
import org.apache.deltaspike.jpa.impl.transaction.context.TransactionContextExtension;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.jpa.api.shared.TestEntityManager;
import org.apache.deltaspike.test.jpa.api.shared.TestEntityTransaction;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class AggregatedDefaultEntityManagerInjectionTest
{
    @Inject
    private TransactionalBean transactionalBean;

    @Inject
    private TestEntityManagerProducer entityManagerProducer;

    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "aggregatedDefaultInjectionTest.jar")
                .addPackage(ArchiveUtils.SHARED_PACKAGE)
                .addPackage(AggregatedDefaultEntityManagerInjectionTest.class.getPackage().getName())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJpaArchive())
                .addAsLibraries(testJar)
                .addAsServiceProvider(Extension.class, TransactionContextExtension.class)
                .addAsWebInfResource(ArchiveUtils.getBeansXml(), "beans.xml");
    }

    @Before
    public void init()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.UnitTest);
    }

    @Test
    public void defaultEntityManagerInjection()
    {
        EntityManager injectedEntityManager = entityManagerProducer.getDefaultEntityManager();

        Assert.assertNotNull(injectedEntityManager);
        Assert.assertTrue(injectedEntityManager instanceof TestEntityManager);
        TestEntityTransaction testTransaction = (TestEntityTransaction) (injectedEntityManager).getTransaction();

        Assert.assertEquals(false, ((TestEntityManager) injectedEntityManager).isFlushed());
        Assert.assertEquals(false, testTransaction.isActive());
        Assert.assertEquals(false, testTransaction.isStarted());
        Assert.assertEquals(false, testTransaction.isCommitted());
        Assert.assertEquals(false, testTransaction.isRolledBack());

        transactionalBean.executeInTransaction();

        Assert.assertEquals(true, ((TestEntityManager) injectedEntityManager).isFlushed());
        Assert.assertEquals(false, testTransaction.isActive());
        Assert.assertEquals(true, testTransaction.isStarted());
        Assert.assertEquals(true, testTransaction.isCommitted());
        Assert.assertEquals(false, testTransaction.isRolledBack());

        Assert.assertEquals(false, TransactionBeanStorage.isOpen());
    }
}
