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
package org.apache.deltaspike.data.impl.tx;

import static org.apache.deltaspike.data.test.util.TestDeployments.initDeployment;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.apache.deltaspike.data.test.domain.Simple;
import org.apache.deltaspike.data.test.service.ExtendedRepositoryInterface;
import org.apache.deltaspike.test.category.WebProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(WebProfileCategory.class)
@RunWith(Arquillian.class)
public class TransactionalQueryRunnerTest
{

    private static final String NAME = "should_run_in_transaction";

    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment()
                .addClasses(ExtendedRepositoryInterface.class)
                .addClass(TransactionalQueryRunnerWrapper.class)
                .addPackage(Simple.class.getPackage());
    }

    @Inject
    private ExtendedRepositoryInterface repository;

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
    @InSequence(4)
    public void should_find_no_lock_without_transaction() throws Exception
    {
        // when
        Simple simple = repository.findByNameNoLock(NAME);

        // then
        assertNotNull(simple);
        assertTrue(wrapper.isRunInNonTx());
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
