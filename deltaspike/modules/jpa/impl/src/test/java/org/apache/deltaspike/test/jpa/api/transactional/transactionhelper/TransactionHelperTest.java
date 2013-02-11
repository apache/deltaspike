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
package org.apache.deltaspike.test.jpa.api.transactional.transactionhelper;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.jpa.api.transaction.TransactionHelper;
import org.apache.deltaspike.jpa.impl.transaction.context.TransactionBeanStorage;
import org.apache.deltaspike.jpa.impl.transaction.context.TransactionContextExtension;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.spi.Extension;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.concurrent.Callable;


@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class TransactionHelperTest
{
    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "defaultInjectionTest.jar")
                .addPackage(ArchiveUtils.SHARED_PACKAGE)
                .addPackage(TransactionHelperTest.class.getPackage().getName())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJpaArchive())
                .addAsLibraries(testJar)
                .addAsServiceProvider(Extension.class, TransactionContextExtension.class)
                .addAsWebInfResource(ArchiveUtils.getBeansXml(), "beans.xml");
    }

    @Test
    public void testTransactionHelper() throws Exception
    {
        try
        {
            resolveEntityManager();
            Assert.fail("ContextNotActiveException expected!");
        }
        catch(ContextNotActiveException cnae)
        {
            // this was expected, all is fine!
        }

        Integer retVal = TransactionHelper.getInstance().executeTransactional( new Callable<Integer>() {

            public Integer call() throws Exception
            {
                resolveEntityManager();

                return Integer.valueOf(3);
            }
        });

        Assert.assertEquals(retVal, Integer.valueOf(3));

        try
        {
            resolveEntityManager();
            Assert.fail("ContextNotActiveException expected!");
        }
        catch(ContextNotActiveException cnae)
        {
            // this was expected, all is fine!
        }

        Assert.assertEquals(false, TransactionBeanStorage.isOpen());
    }

    private void resolveEntityManager()
    {
        EntityManager em = BeanProvider.getContextualReference(EntityManager.class);
        Assert.assertNotNull(em);
        EntityTransaction et = em.getTransaction();
        Assert.assertNotNull(et);
    }
}

