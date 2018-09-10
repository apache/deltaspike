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

import org.apache.deltaspike.jpa.impl.transaction.ManagedUserTransactionResolver;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

@Specializes
@ApplicationScoped
public class MockUserTransactionResolver extends ManagedUserTransactionResolver {

    private MockUserTransaction mockTx;

    @PostConstruct
    public void resetTx() {
        mockTx = new MockUserTransaction();
    }

    @Override
    public MockUserTransaction resolveUserTransaction() {
        return mockTx;
    }

    public static class MockUserTransaction implements UserTransaction {
        private boolean begin = false;
        private boolean commit = false;
        private boolean rollback = false;
        private boolean rollBackOnly =false;

        private int status = Status.STATUS_NO_TRANSACTION;


        @Override
        public void begin() throws NotSupportedException, SystemException {
            this.begin = true;
            this.status = Status.STATUS_ACTIVE;
        }

        @Override
        public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
            this.commit = true;
            this.status = Status.STATUS_COMMITTED;
        }

        @Override
        public int getStatus() throws SystemException {
            return status;
        }

        @Override
        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            this.rollback = true;
            this.status = Status.STATUS_ROLLEDBACK;
        }

        @Override
        public void setRollbackOnly() throws IllegalStateException, SystemException {
            this.rollBackOnly = true;
            this.status = Status.STATUS_MARKED_ROLLBACK;
        }

        @Override
        public void setTransactionTimeout(int i) throws SystemException {
            // do nothing
        }

        public boolean isActive()
        {
            return begin && !(commit || rollback);
        }

        public boolean isBegin() {
            return begin;
        }

        public boolean isCommit() {
            return commit;
        }

        public boolean isRollback() {
            return rollback;
        }

        public boolean isRollBackOnly() {
            return rollBackOnly;
        }
    }
}
