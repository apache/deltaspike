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
package org.apache.deltaspike.data.test.ee7.tx;

import static org.apache.deltaspike.data.test.ee7.util.TestDeployments.initDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.deltaspike.data.test.ee7.domain.Simple;
import org.apache.deltaspike.data.test.ee7.service.JtaTransactionalRepositoryAbstract;
import org.apache.deltaspike.data.test.ee7.service.SimpleClientApp;
import org.apache.deltaspike.data.test.ee7.service.SimpleClientDep;
import org.apache.deltaspike.data.test.ee7.service.SimpleClientTx;
import org.apache.deltaspike.test.category.WebEE7ProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Tests transactional repositories with the {@code javax.transaction.Transactional} interceptor
 * binding and {@code javax.transaction.TransactionScoped} beans.
 * <p>
 * The test uses groups of beans with identical collaboration, only differing by the scope one bean
 * per group ({@code SimpleHolderTx} vs. {@code SimpleHolderApp} vs. {@code SimpleHolderDep} ), to
 * verify that transaction scoped beans actually do behave in a different way, compared to
 * application scoped or dependent beans.
 */
@Category(WebEE7ProfileCategory.class)
@RunWith(Arquillian.class)
@Ignore("Due to injection point issue in Wildfly 10 - DELTASPIKE-1060")
public class JtaTransactionalRepositoryAbstractTest
{

    public static String DS_PROPERTIES_WITH_CMT_STRATEGY = "globalAlternatives.org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy="
        + "org.apache.deltaspike.jpa.impl.transaction.ContainerManagedTransactionStrategy";

    private static final String NAME = "should_run_in_transaction";

    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment()
            .addPackage(JtaTransactionalRepositoryAbstract.class.getPackage())
            .addClass(TransactionalQueryRunnerWrapper.class)
            .addPackage(Simple.class.getPackage())
            .addAsWebInfResource(new StringAsset(DS_PROPERTIES_WITH_CMT_STRATEGY),
                "classes/META-INF/apache-deltaspike.properties");

    }

    @Inject
    private JtaTransactionalRepositoryAbstract repository;

    @Produces
    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private TransactionalQueryRunnerWrapper wrapper;

    @Inject
    private SimpleClientTx simpleClientTx;

    @Inject
    private SimpleClientApp simpleClientApp;

    @Inject
    private SimpleClientDep simpleClientDep;

    @Test
    @InSequence(1)
    public void should_run_modifying_in_transaction() throws Exception
    {
        // when
        repository.deleteAll();

        // then
        assertTrue(wrapper.isRunInTx());
    }

    @Test
    @InSequence(2)
    public void should_save_in_transaction() throws Exception
    {
        // given
        Simple simple = new Simple(NAME);

        // when
        simple = repository.save(simple);

        // then
        assertNotNull(simple.getId());
        assertTrue(wrapper.isRunInTx());
    }

    @Test
    @InSequence(3)
    public void should_find_with_lockmode_in_transaction() throws Exception
    {
        // when
        Simple simple = repository.findOptionalByName(NAME);

        // then
        assertNotNull(simple);
        assertTrue(wrapper.isRunInTx());
    }

    @Test
    @InSequence(10)
    public void tx_scoped_bean_should_be_empty_before_tx() throws Exception
    {
        // when
        Simple simple = simpleClientTx.getSimple();

        // then
        assertNull(simple);
    }

    @Test
    @InSequence(11)
    public void should_save_when_tx_scoped_bean_is_found() throws Exception
    {
        // when
        Simple simple = simpleClientTx.createSimple("transaction scoped");
        Simple found = repository.findOptionalByName("transaction scoped");

        // then
        assertNotNull(simple);
        assertNotNull(found);
        assertEquals("transaction scoped", found.getName());
    }

    @Test
    @InSequence(12)
    public void tx_scoped_bean_should_be_empty_after_tx() throws Exception
    {
        // when
        Simple simple = simpleClientTx.getSimple();

        // then
        assertNull(simple);
    }

    @Test
    @InSequence(20)
    public void app_scoped_bean_should_be_empty_before_tx() throws Exception
    {
        // when
        Simple simple = simpleClientApp.getSimple();

        // then
        assertNull(simple);
    }

    @Test
    @InSequence(21)
    public void should_save_when_app_scoped_bean_is_found() throws Exception
    {
        // when
        Simple simple = simpleClientApp.createSimple("application scoped");
        Simple found = repository.findOptionalByName("application scoped");

        // then
        assertNotNull(simple);
        assertNotNull(found);
        assertEquals("application scoped", found.getName());
    }

    @Test
    @InSequence(22)
    public void app_scoped_bean_should_not_be_empty_after_tx() throws Exception
    {
        // when
        Simple simple = simpleClientApp.getSimple();

        // then
        assertNotNull(simple);
    }

    @Test
    @InSequence(30)
    public void dep_scoped_bean_should_be_empty_before_tx() throws Exception
    {
        // when
        Simple simple = simpleClientDep.getSimple();

        // then
        assertNull(simple);
    }

    @Test
    @InSequence(31)
    public void should_save_when_dep_scoped_bean_is_found() throws Exception
    {
        // when
        Simple simple = simpleClientDep.createSimple("dependent");
        Simple found = repository.findOptionalByName("dependent");

        // then
        assertNull(simple);
        assertNull(found);
    }

    @Test
    @InSequence(32)
    public void dep_scoped_bean_should_be_empty_after_tx() throws Exception
    {
        // when
        Simple simple = simpleClientDep.getSimple();

        // then
        assertNotNull(simple);
    }

    @Test
    @InSequence(100)
    public void should_cleanup() throws Exception
    {
        repository.deleteAll();
    }

    @Before
    public void init()
    {
        wrapper.reset();
    }
}
