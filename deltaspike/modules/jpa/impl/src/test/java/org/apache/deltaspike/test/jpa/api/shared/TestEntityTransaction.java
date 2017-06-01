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

import javax.persistence.EntityTransaction;

public class TestEntityTransaction implements EntityTransaction
{
    private boolean started = false;
    private boolean committed = false;
    private boolean rolledBack = false;
    private boolean markRolledBack = false;
    private TestEntityManager testEntityManager;

    public TestEntityTransaction(TestEntityManager testEntityManager)
    {
        this.testEntityManager = testEntityManager;
    }

    @Override
    public void begin()
    {
        if (started)
        {
            throw new IllegalStateException("transaction started already");
        }

        started = true;
    }

    @Override
    public void commit()
    {
        committed = true;
        testEntityManager.setFlushed(true);
    }

    @Override
    public void rollback()
    {
        rolledBack = true;
    }

    @Override
    public void setRollbackOnly()
    {
        this.markRolledBack = true;
    }

    @Override
    public boolean getRollbackOnly()
    {
        return this.markRolledBack;
    }

    @Override
    public boolean isActive()
    {
        return started && !(committed || rolledBack);
    }

    public boolean isStarted()
    {
        return started;
    }

    public boolean isCommitted()
    {
        return committed;
    }

    public boolean isRolledBack()
    {
        return rolledBack;
    }
}
