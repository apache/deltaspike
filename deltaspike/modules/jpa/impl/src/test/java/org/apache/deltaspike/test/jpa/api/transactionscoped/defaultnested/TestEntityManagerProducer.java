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
package org.apache.deltaspike.test.jpa.api.transactionscoped.defaultnested;

import org.apache.deltaspike.jpa.api.transaction.TransactionScoped;
import org.apache.deltaspike.test.jpa.api.shared.TestEntityManager;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;

@RequestScoped
public class TestEntityManagerProducer
{
    private TestEntityManager entityManager;

    private int closeEntityManagerCount = 0;

    @Produces
    @TransactionScoped
    protected EntityManager entityManager()
    {
        if (entityManager == null)
        {
            entityManager = new TestEntityManager();
            return entityManager;
        }

        throw new IllegalStateException("a second producer call isn't allowed");
    }

    protected void closeEntityManager(@Disposes EntityManager entityManager)
    {
        if (entityManager.isOpen())
        {
            entityManager.close();
        }
        closeEntityManagerCount++;
    }

    public int getCloseEntityManagerCount()
    {
        return closeEntityManagerCount;
    }

    public TestEntityManager getEntityManager()
    {
        return entityManager;
    }
}
