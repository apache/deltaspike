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
package org.apache.deltaspike.test.jpa.impl.entitymanager;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.deltaspike.jpa.impl.entitymanager.EntityManagerMetadata;
import org.apache.deltaspike.jpa.impl.entitymanager.EntityManagerRef;
import org.apache.deltaspike.jpa.impl.entitymanager.EntityManagerRefLookup;
import org.apache.deltaspike.jpa.impl.transaction.context.TransactionContextExtension;
import org.apache.deltaspike.jpa.spi.entitymanager.ActiveEntityManagerHolder;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.persistence.EntityManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class EntityManagerRefLookupTest
{
    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "defaultInjectionTest.jar")
                .addPackage(ArchiveUtils.SHARED_PACKAGE)
                .addPackage(org.apache.deltaspike.test.jpa.api.transactional.transactionhelper.TransactionHelperTest.class.getPackage().getName())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJpaArchive())
                .addAsLibraries(testJar)
                .addAsServiceProvider(Extension.class, TransactionContextExtension.class)
                .addAsWebInfResource(ArchiveUtils.getBeansXml(), "beans.xml");
    }

    private EntityManagerRefLookup emRefLookup = new EntityManagerRefLookup();

    @Before
    public void setup() throws IllegalAccessException {
        FieldUtils.writeField(emRefLookup, "activeEntityManagerHolder", new ActiveEntityManagerHolder() {
            @Override
            public void set(EntityManager entityManager) {

            }

            @Override
            public boolean isSet() {
                return false;
            }

            @Override
            public EntityManager get() {
                return null;
            }

            @Override
            public void dispose() {

            }
        }, true);
    }

    @Test
    public void entity_manager_initialized() throws InterruptedException
    {
        final AtomicBoolean emNotNull = new AtomicBoolean(true);
        final ExecutorService executorService = Executors.newFixedThreadPool(50);
        for(int threadCount = 0 ; threadCount < 50 ; threadCount++) {
            executorService.execute(
                    () -> {
                        EntityManagerRef entityManagerRef = emRefLookup.lookupReference(new EntityManagerMetadata());
                        if (entityManagerRef.getEntityManager() == null) {
                            emNotNull.set(false);
                        }
                    }
            );
        }
        executorService.shutdown();
        executorService.awaitTermination(10000, TimeUnit.MILLISECONDS);
        assertTrue("Entity manager should be initialized, but isn't", emNotNull.get());
    }
}
