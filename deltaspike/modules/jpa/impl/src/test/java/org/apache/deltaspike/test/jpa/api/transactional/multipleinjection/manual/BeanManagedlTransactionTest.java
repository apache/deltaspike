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
package org.apache.deltaspike.test.jpa.api.transactional.multipleinjection.manual;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.jpa.impl.transaction.context.TransactionContextExtension;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;


/**
 * Same test as {@link ManualTransactionTest} but now with a UserTransaction instead
 * of manual EM Tx.
 */
@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class BeanManagedlTransactionTest
{
    private static Asset beansXml = new StringAsset(
            "<beans>" +
            "<alternatives>" +
            "<class>org.apache.deltaspike.jpa.impl.transaction.BeanManagedUserTransactionStrategy</class>" +
            "</alternatives>" +
            "</beans>"
    );


    @Inject
    private ManualTransactionBean manualTransactionBean;

    @Inject
    private MockUserTransactionResolver mockTxResolver;

    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "manualTransactionTest.jar")
                .addPackage(ArchiveUtils.SHARED_PACKAGE)
                .addPackage(BeanManagedlTransactionTest.class.getPackage().getName())
                .addAsManifestResource(beansXml, "beans.xml");

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
    public void manualTransactionTest()
    {

        mockTxResolver.resetTx();
        MockUserTransactionResolver.MockUserTransaction mockTx = mockTxResolver.resolveUserTransaction();
        manualTransactionBean.executeInTransaction();

        Assert.assertEquals(false, mockTx.isActive());
        Assert.assertEquals(true, mockTx.isBegin());
        Assert.assertEquals(true, mockTx.isCommit());
        Assert.assertEquals(false, mockTx.isRollback());
        Assert.assertEquals(false, mockTx.isRollBackOnly());
    }
}
