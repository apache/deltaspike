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

import org.apache.deltaspike.jpa.impl.transaction.context.EntityManagerEntry;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.lang.annotation.Annotation;

/**
 * <p>This alternative {@link org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy} uses auto-detection and
 * can be used for different (parallel) persistence-units which use different transaction-types or
 * if different environments (dev., prod.,...) should use different transaction-types.</p>
 *
 * <p>This implementation can be used for environments which allow a mixed usage of JTA and RESOURCE_LOCAL.
 * (Within a transactional call it isn't possible to mix different transaction-types.)<b/>
 *
 * E.g.: in an application-server this class allows to use a persistence-unit with
 * transaction-type="RESOURCE_LOCAL" + non-jta-data-source
 * in parallel to a persistence-unit with
 * transaction-type="JTA" + jta-data-source</p>
 *
 * <p>Optional:<br/>
 * E.g. in case of a project-stage based logic
 * {@link org.apache.deltaspike.core.api.exclude.Exclude} can be used to switch between different
 * producer-beans.</p>
 *
 * <p>It's a better alternative than extending
 * {@link BeanManagedUserTransactionStrategy}
 * (which would lead to an impl. dependency) only for using
 * {@link org.apache.deltaspike.core.api.exclude.Exclude} at the custom
 * {@link org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy}
 * (or doing a custom veto-extension).</p>
 */
@Dependent
@Alternative
@SuppressWarnings("UnusedDeclaration")
//TODO depending on further discussions about an own JTA module, BeanManagedUserTransactionStrategy
//could be the default (via @Specializes) in the ds-jta module.
//Depending on further discussions this class can be merged with BeanManagedUserTransactionStrategy or
//we keep BeanManagedUserTransactionStrategy separated as a small tweak for applications which only use JTA transactions
//or as a base implementation for a custom EnvironmentAwareTransactionStrategy.
public class EnvironmentAwareTransactionStrategy extends BeanManagedUserTransactionStrategy
{
    private static final long serialVersionUID = -4432802805095533499L;

    private static ThreadLocal<Boolean> isJtaModeDetected = new ThreadLocal<Boolean>();

    @Override
    protected EntityManagerEntry createEntityManagerEntry(
            EntityManager entityManager, Class<? extends Annotation> qualifier)
    {
        boolean isTransactionTypeJta = false;

        //Ensures that nested transactional beans with different entity-managers don't lead to a rollback
        //(in case of transaction-type JTA).
        if (isJtaModeDetected.get() == null)
        {
            try
            {
                //This check is only valid here, because the transaction isn't started.
                entityManager.getTransaction();
            }
            catch (IllegalStateException e)
            {
                isTransactionTypeJta = true;
            }
            isJtaModeDetected.set(isTransactionTypeJta);
        }
        else
        {
            isTransactionTypeJta = isInJtaTransaction();
        }

        if (isTransactionTypeJta)
        {
            applyTransactionTimeout(); //needs to be done before UserTransaction#begin - TODO move this call
        }
        return new EntityManagerEntry(entityManager, qualifier);
    }

    @Override
    protected void beforeProceed(InvocationContext invocationContext,
                                 EntityManagerEntry entityManagerEntry,
                                 EntityTransaction transaction)
    {
        if (isInJtaTransaction())
        {
            super.beforeProceed(invocationContext, entityManagerEntry, transaction);
        }
    }

    @Override
    protected EntityTransaction getTransaction(EntityManagerEntry entityManagerEntry)
    {
        if (isInJtaTransaction())
        {
            return super.getTransaction(entityManagerEntry);
        }

        return entityManagerEntry.getEntityManager().getTransaction();
    }

    @Override
    protected void onCloseTransactionScope()
    {
        super.onCloseTransactionScope();
        isJtaModeDetected.set(null);
        isJtaModeDetected.remove();
    }

    private static boolean isInJtaTransaction()
    {
        return Boolean.TRUE.equals(isJtaModeDetected.get());
    }
}
