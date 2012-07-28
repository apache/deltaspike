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
package org.apache.deltaspike.jpa.impl.transaction;

import org.apache.deltaspike.core.impl.util.JndiUtils;
import org.apache.deltaspike.jpa.impl.transaction.context.EntityManagerEntry;
import org.apache.deltaspike.jpa.impl.transaction.context.JtaEntityManagerEntry;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.UserTransaction;
import java.lang.annotation.Annotation;
import java.util.logging.Logger;

/**
 * This alternative {@link org.apache.deltaspike.jpa.spi.PersistenceStrategy} uses a simple auto-detection
 * based on a failed JNDI lookup (of java:comp/UserTransaction) and
 * can be used if different environments (dev., prod.,...) should use different transaction-types.
 *
 * It's a better alternative than extending
 * {@link org.apache.deltaspike.jpa.impl.transaction.BeanManagedUserTransactionPersistenceStrategy}
 * (which would lead to an impl. dependency) only for using
 * {@link org.apache.deltaspike.core.api.exclude.annotation.Exclude}
 * (or doing a custom veto-extension).
 */
@Dependent
@Alternative
@SuppressWarnings("UnusedDeclaration")
public class EnvironmentAwarePersistenceStrategy extends BeanManagedUserTransactionPersistenceStrategy
{
    private static final long serialVersionUID = -3432802805095533499L;

    private static final Logger LOGGER =
        Logger.getLogger(EnvironmentAwarePersistenceStrategy.class.getName());

    @Override
    protected EntityManagerEntry createEntityManagerEntry(
        EntityManager entityManager, Class<? extends Annotation> qualifier)
    {
        try
        {
            //just used for detecting the environment (e.g. servlet-container vs. application-server)
            JndiUtils.lookup(USER_TRANSACTION_JNDI_NAME, UserTransaction.class);
        }
        catch (IllegalStateException e)
        {
            return new JtaEntityManagerEntry(entityManager, qualifier, false);
        }
        return super.createEntityManagerEntry(entityManager, qualifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void beforeProceed(EntityManagerEntry entityManagerEntry)
    {
        //cast without check is valid, because the entry was created by this class - see #createEntityManagerEntry
        if (((JtaEntityManagerEntry)entityManagerEntry).isTransactionTypeJta())
        {
            super.beforeProceed(entityManagerEntry);
        }
    }

    @Override
    protected EntityTransaction getTransaction(EntityManagerEntry entityManagerEntry)
    {
        if (((JtaEntityManagerEntry)entityManagerEntry).isTransactionTypeJta())
        {
            return super.getTransaction(entityManagerEntry);
        }

        return entityManagerEntry.getEntityManager().getTransaction();
    }
}
