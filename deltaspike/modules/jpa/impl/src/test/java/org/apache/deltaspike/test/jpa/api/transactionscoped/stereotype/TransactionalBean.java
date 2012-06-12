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
package org.apache.deltaspike.test.jpa.api.transactionscoped.stereotype;

import org.apache.deltaspike.test.jpa.api.shared.TestEntityTransaction;

import javax.inject.Inject;
import javax.persistence.EntityManager;

@Repository
public class TransactionalBean
{
    @Inject
    private EntityManager entityManager;

    //we can't provide a simple getter because it would be intercepted as well and
    //would break the check we have to perform in the test
    @Inject
    private TestEntityTransactionHolder testEntityTransactionHolder;

    public void executeInTransaction()
    {
        testEntityTransactionHolder.setTestEntityTransaction(
                (TestEntityTransaction) entityManager.getTransaction());
    }
}
