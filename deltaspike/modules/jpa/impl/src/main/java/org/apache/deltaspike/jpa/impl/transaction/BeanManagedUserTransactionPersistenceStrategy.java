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
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.jpa.impl.transaction.context.EntityManagerEntry;
import org.apache.deltaspike.jpa.impl.transaction.context.JtaEntityManagerEntry;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.lang.annotation.Annotation;

/**
 * <p>{@link org.apache.deltaspike.jpa.spi.PersistenceStrategy} for using JTA (bean-managed-)transactions
 * (including XA transactions with a XA DataSource).
 * The basic features are identical to the {@link ResourceLocalPersistenceStrategy} (for
 * persistent-unit-transaction-type 'RESOURCE_LOCAL' only).
 * Also different transaction-types for different persistence-units are supported.</p>
 *
 * <p>It's possible to extend this class, if {@link org.apache.deltaspike.core.api.exclude.annotation.Exclude}
 * needs to be used e.g. in case of a different dev- and production-environment
 * (in combination with different {@link javax.persistence.EntityManagerFactory}s).</p>
 */
@Dependent
@Alternative
@SuppressWarnings("UnusedDeclaration")
//TODO move to a separated ds-jta module and use @Specializes -> no additional config is needed
public class BeanManagedUserTransactionPersistenceStrategy extends ResourceLocalPersistenceStrategy
{
    protected static final String USER_TRANSACTION_JNDI_NAME = "java:comp/UserTransaction";

    private static final long serialVersionUID = -2432802805095533499L;

    @Override
    protected EntityManagerEntry createEntityManagerEntry(
        EntityManager entityManager, Class<? extends Annotation> qualifier)
    {
        return new JtaEntityManagerEntry(entityManager, qualifier);
    }

    @Override
    protected EntityTransaction getTransaction(EntityManagerEntry entityManagerEntry)
    {
        return new UserTransactionAdapter(entityManagerEntry.getEntityManager());
    }

    /**
     * Needed because the {@link EntityManager} was created outside of the {@link UserTransaction}.
     * Can't be in {@link BeanManagedUserTransactionPersistenceStrategy.UserTransactionAdapter#begin()}
     * because {@link ResourceLocalPersistenceStrategy} needs to do
     * <pre>
     * if (!transaction.isActive())
     * {
     *     transaction.begin();
     * }
     * </pre>
     * for the {@link EntityTransaction} of every {@link EntityManager}
     * and {@link BeanManagedUserTransactionPersistenceStrategy.UserTransactionAdapter#isActive()}
     * can only use the status information of the {@link UserTransaction} and therefore
     * {@link BeanManagedUserTransactionPersistenceStrategy.UserTransactionAdapter#begin()}
     * will only executed once, but {@link javax.persistence.EntityManager#joinTransaction()}
     * needs to be called for every {@link EntityManager}
     *
     * @param entityManagerEntry entry of the current entity-manager
     */

    @Override
    protected void beforeProceed(EntityManagerEntry entityManagerEntry)
    {
        entityManagerEntry.getEntityManager().joinTransaction();
    }

    private class UserTransactionAdapter implements EntityTransaction
    {
        private final UserTransaction userTransaction;
        private final EntityManager entityManager;

        public UserTransactionAdapter(EntityManager entityManager)
        {
            this.userTransaction = JndiUtils.lookup(USER_TRANSACTION_JNDI_NAME, UserTransaction.class);
            this.entityManager = entityManager;
        }

        /**
         * Only delegate to the {@link UserTransaction} if the state of the
         * {@link UserTransaction} is {@link Status#STATUS_NO_TRANSACTION}
         * (= the status before and after a started transaction).
         */
        @Override
        public void begin()
        {
            try
            {
                //2nd check (already done by #isActive triggered by ResourceLocalPersistenceStrategy directly before)
                //currently to filter STATUS_UNKNOWN - see to-do -> TODO re-visit it
                if (this.userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION)
                {
                    this.userTransaction.begin();
                }
            }
            catch (Exception e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }

        /**
         * Only delegate to the {@link UserTransaction} if the state of the
         * {@link UserTransaction} is one of
         * <ul>
         *     <li>{@link Status#STATUS_ACTIVE}</li>
         *     <li>{@link Status#STATUS_PREPARING}</li>
         *     <li>{@link Status#STATUS_PREPARED}</li>
         * </ul>
         */
        @Override
        public void commit()
        {
            try
            {
                if (isTransactionReadyToCommit())
                {
                    this.userTransaction.commit();
                }
            }
            catch (Exception e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }

        /**
         * Only delegate to the {@link UserTransaction} if the state of the
         * {@link UserTransaction} is one of
         * <ul>
         *     <li>{@link Status#STATUS_ACTIVE}</li>
         *     <li>{@link Status#STATUS_PREPARING}</li>
         *     <li>{@link Status#STATUS_PREPARED}</li>
         *     <li>{@link Status#STATUS_MARKED_ROLLBACK}</li>
         *     <li>{@link Status#STATUS_COMMITTING}</li>
         * </ul>
         */
        @Override
        public void rollback()
        {
            try
            {
                if (isTransactionAllowedToRollback())
                {
                    this.userTransaction.rollback();
                }
            }
            catch (SystemException e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }

        @Override
        public void setRollbackOnly()
        {
            try
            {
                this.userTransaction.setRollbackOnly();
            }
            catch (SystemException e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }

        @Override
        public boolean getRollbackOnly()
        {
            try
            {
                return this.userTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK;
            }
            catch (SystemException e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }

        /**
         * @return true if the transaction has been started and not ended
         */
        @Override
        public boolean isActive()
        {
            //we can't use the status of the overall
            try
            {
                return this.userTransaction.getStatus() != Status.STATUS_NO_TRANSACTION &&
                        this.userTransaction.getStatus() != Status.STATUS_UNKNOWN; //TODO re-visit it
            }
            catch (SystemException e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }

        private boolean isTransactionAllowedToRollback() throws SystemException
        {
            return isTransactionReadyToCommit() ||
                    this.userTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK ||
                    this.userTransaction.getStatus() == Status.STATUS_COMMITTING;
        }

        private boolean isTransactionReadyToCommit() throws SystemException
        {
            return this.userTransaction.getStatus() == Status.STATUS_ACTIVE ||
                    this.userTransaction.getStatus() == Status.STATUS_PREPARING ||
                    this.userTransaction.getStatus() == Status.STATUS_PREPARED;
        }
    }
}
