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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.deltaspike.data.test.ee7.domain.Simple;
import org.apache.deltaspike.data.test.ee7.service.DeltaSpikeTransactionalRepositoryInterface;
import org.apache.deltaspike.test.category.WebEE7ProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(WebEE7ProfileCategory.class)
@RunWith(Arquillian.class)
public class DeltaSpikeTransactionalRepositoryInterfaceTest
{

    public static String DS_PROPERTIES_WITH_ENV_AWARE_TX_STRATEGY
            = "globalAlternatives.org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy="
            + "org.apache.deltaspike.jpa.impl.transaction.EnvironmentAwareTransactionStrategy";
    
    private static final String NAME = "should_run_in_transaction";

    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment()
                .addClass(DeltaSpikeTransactionalRepositoryInterface.class)
                .addClass(TransactionalQueryRunnerWrapper.class)
                .addPackage(Simple.class.getPackage())
                .addAsWebInfResource(new StringAsset(DS_PROPERTIES_WITH_ENV_AWARE_TX_STRATEGY),
                    "classes/META-INF/apache-deltaspike.properties");
    }

    @Inject
    private DeltaSpikeTransactionalRepositoryInterface repository;

    @Produces
    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private TransactionalQueryRunnerWrapper wrapper;

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
        Simple simple = repository.findByName(NAME);

        // then
        assertNotNull(simple);
        assertTrue(wrapper.isRunInTx());
    }

    @Test
    @InSequence(10)
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
