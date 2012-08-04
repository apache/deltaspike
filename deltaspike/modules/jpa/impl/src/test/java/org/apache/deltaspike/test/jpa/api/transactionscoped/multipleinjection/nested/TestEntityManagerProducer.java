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
package org.apache.deltaspike.test.jpa.api.transactionscoped.multipleinjection.nested;

import org.apache.deltaspike.jpa.api.transaction.TransactionScoped;
import org.apache.deltaspike.test.jpa.api.shared.First;
import org.apache.deltaspike.test.jpa.api.shared.Second;
import org.apache.deltaspike.test.jpa.api.shared.TestEntityManager;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;

@RequestScoped
public class TestEntityManagerProducer
{
    private TestEntityManager firstEntityManager;

    private TestEntityManager secondEntityManager;

    private int closeEntityManagerCountFirstEntityManager = 0;

    private int closeEntityManagerCountSecondEntityManager = 0;

    @Produces
    @First
    @TransactionScoped
    protected EntityManager firstEntityManager()
    {
        if (firstEntityManager == null)
        {
            firstEntityManager = new TestEntityManager();
            return firstEntityManager;
        }

        throw new IllegalStateException("a second producer call isn't allowed");
    }

    @Produces
    @Second
    @TransactionScoped
    protected EntityManager secondEntityManager()
    {
        if (secondEntityManager == null)
        {
            secondEntityManager = new TestEntityManager();
            return secondEntityManager;
        }

        throw new IllegalStateException("a second producer call isn't allowed");
    }

    protected void closeFirstEntityManager(@Disposes @First EntityManager entityManager)
    {
        if (entityManager.isOpen())
        {
            entityManager.close();
        }
        closeEntityManagerCountFirstEntityManager++;
    }

    protected void closeSecondEntityManager(@Disposes @Second EntityManager entityManager)
    {
        if (entityManager.isOpen())
        {
            entityManager.close();
        }
        closeEntityManagerCountSecondEntityManager++;
    }

    public TestEntityManager getFirstEntityManager()
    {
        return firstEntityManager;
    }

    public TestEntityManager getSecondEntityManager()
    {
        return secondEntityManager;
    }

    public int getCloseEntityManagerCountFirstEntityManager()
    {
        return closeEntityManagerCountFirstEntityManager;
    }

    public int getCloseEntityManagerCountSecondEntityManager()
    {
        return closeEntityManagerCountSecondEntityManager;
    }
}
