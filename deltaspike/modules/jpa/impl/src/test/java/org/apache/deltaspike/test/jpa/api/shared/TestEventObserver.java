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
package org.apache.deltaspike.test.jpa.api.shared;

import org.apache.deltaspike.jpa.impl.transaction.PersistenceStrategyCleanupTestEvent;
import org.apache.deltaspike.jpa.impl.transaction.TransactionBeanStorageCleanupTestEvent;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;

@RequestScoped
public class TestEventObserver
{
    private int persistenceStrategyCleanupCalls = 0;
    private int transactionBeanStorageCleanupCalls = 0;

    protected void onPersistenceStrategyCleanup(
        @Observes PersistenceStrategyCleanupTestEvent persistenceStrategyCleanupTestEvent)
    {
        this.persistenceStrategyCleanupCalls++;
    }

    protected void onTransactionBeanStorageCleanup(
        @Observes TransactionBeanStorageCleanupTestEvent transactionBeanStorageCleanupTestEvent)
    {
        this.transactionBeanStorageCleanupCalls++;
    }

    public int getPersistenceStrategyCleanupCalls()
    {
        return persistenceStrategyCleanupCalls;
    }

    public int getTransactionBeanStorageCleanupCalls()
    {
        return transactionBeanStorageCleanupCalls;
    }
}
