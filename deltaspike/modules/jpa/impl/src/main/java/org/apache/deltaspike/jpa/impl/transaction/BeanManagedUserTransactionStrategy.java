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

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.impl.util.JndiUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.jpa.api.transaction.TransactionConfig;
import org.apache.deltaspike.jpa.impl.transaction.context.EntityManagerEntry;

import javax.annotation.Resource;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>{@link org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy} for using JTA (bean-managed-)transactions
 * (including XA transactions with a XA DataSource).
 * The basic features are identical to the {@link ResourceLocalTransactionStrategy} (for
 * persistent-unit-transaction-type 'RESOURCE_LOCAL' only).</p>
 */
@Dependent
@Alternative
@SuppressWarnings("UnusedDeclaration")
//TODO move to a separated ds-jta module and use @Specializes -> no additional config is needed
public class BeanManagedUserTransactionStrategy extends ResourceLocalTransactionStrategy
{
    protected static final String USER_TRANSACTION_JNDI_NAME = "java:comp/UserTransaction";
    protected static final String TRANSACTION_SYNC_REGISTRY_JNDI_NAME = "java:comp/TransactionSynchronizationRegistry";

    private static final long serialVersionUID = -2432802805095533499L;

    private static final Logger LOGGER = Logger.getLogger(BeanManagedUserTransactionStrategy.class.getName());

    private transient TransactionConfig transactionConfig;

    @Resource
    private UserTransaction userTransaction;

    @Override
    protected EntityManagerEntry createEntityManagerEntry(
        EntityManager entityManager, Class<? extends Annotation> qualifier)
    {
        applyTransactionTimeout(); //needs to be done before UserTransaction#begin - TODO move this call
        return super.createEntityManagerEntry(entityManager, qualifier);
    }

    protected void applyTransactionTimeout()
    {
        Integer transactionTimeout = getDefaultTransactionTimeoutInSeconds();

        if (transactionTimeout == null)
        {
            //the default configured for the container will be used
            return;
        }

        try
        {
            UserTransaction userTransaction = resolveUserTransaction();

            if (userTransaction == null)
            {
                // if there is a CMT EJB call active, then we do not set any timeout
                return;
            }

            if (userTransaction.getStatus() != Status.STATUS_ACTIVE)
            {
                userTransaction.setTransactionTimeout(transactionTimeout);
            }
        }
        catch (SystemException e)
        {
            LOGGER.log(Level.WARNING, "UserTransaction#setTransactionTimeout failed", e);
        }
    }

    protected Integer getDefaultTransactionTimeoutInSeconds()
    {
        if (this.transactionConfig == null)
        {
            lazyInit();
        }

        return transactionConfig.getUserTransactionTimeoutInSeconds();
    }

    protected synchronized void lazyInit()
    {
        if (this.transactionConfig != null)
        {
            return;
        }

        this.transactionConfig = BeanProvider.getContextualReference(TransactionConfig.class, true);

        if (this.transactionConfig == null)
        {
            this.transactionConfig = createDefaultTransactionConfig();
        }
    }

    protected TransactionConfig createDefaultTransactionConfig()
    {
        return new TransactionConfig()
        {
            private static final long serialVersionUID = -3915439087580270117L;

            @Override
            public Integer getUserTransactionTimeoutInSeconds()
            {
                return null;
            }
        };
    }

    @Override
    protected EntityTransaction getTransaction(EntityManagerEntry entityManagerEntry)
    {
        return new UserTransactionAdapter();
    }

    /**
     * Needed because the {@link EntityManager} might get created outside of the {@link UserTransaction}
     * (e.g. depending on the implementation of the producer).
     * Can't be in {@link BeanManagedUserTransactionStrategy.UserTransactionAdapter#begin()}
     * because {@link ResourceLocalTransactionStrategy} needs to do
     * <pre>
     * if (!transaction.isActive())
     * {
     *     transaction.begin();
     * }
     * </pre>
     * for the {@link EntityTransaction} of every {@link EntityManager}
     * and {@link BeanManagedUserTransactionStrategy.UserTransactionAdapter#isActive()}
     * can only use the status information of the {@link UserTransaction} and therefore
     * {@link BeanManagedUserTransactionStrategy.UserTransactionAdapter#begin()}
     * will only executed once, but {@link javax.persistence.EntityManager#joinTransaction()}
     * needs to be called for every {@link EntityManager}.
     *
     * @param entityManagerEntry entry of the current entity-manager
     */
    @Override
    protected void beforeProceed(EntityManagerEntry entityManagerEntry)
    {
        entityManagerEntry.getEntityManager().joinTransaction();
    }

    protected UserTransaction resolveUserTransaction()
    {
        if (userTransaction != null)
        {
            return userTransaction;
        }

        try
        {
            return JndiUtils.lookup(USER_TRANSACTION_JNDI_NAME, UserTransaction.class);
        }
        catch (Exception ne)
        {
            // do nothing it was just a try
            return null;
        }
    }

    protected TransactionSynchronizationRegistry resolveTransactionRegistry()
    {
        return JndiUtils.lookup(TRANSACTION_SYNC_REGISTRY_JNDI_NAME, TransactionSynchronizationRegistry.class);
    }

    private class UserTransactionAdapter implements EntityTransaction
    {
        private final UserTransaction userTransaction;
        private TransactionSynchronizationRegistry transactionSynchronizationRegistry = null;

        public UserTransactionAdapter()
        {
            this.userTransaction = resolveUserTransaction();

            if (this.userTransaction == null)
            {
                transactionSynchronizationRegistry = resolveTransactionRegistry();

                if (transactionSynchronizationRegistry.getTransactionStatus() != Status.STATUS_ACTIVE)
                {
                    throw new RuntimeException("invalid state/badly configured JTA datasource");
                }
            }
        }

        /**
         * Only delegate to the {@link UserTransaction} if the state of the
         * {@link UserTransaction} is {@link Status#STATUS_NO_TRANSACTION}
         * (= the status before and after a started transaction).
         */
        @Override
        public void begin()
        {

            if (this.userTransaction == null)
            {
                throw new IllegalStateException("cannot begin UserTransaction in CMT environment");
            }

            try
            {
                //2nd check (already done by #isActive triggered by ResourceLocalTransactionStrategy directly before)
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
            if (this.userTransaction == null)
            {
                throw new IllegalStateException("cannot commit UserTransaction in CMT environment");
            }


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
            if (this.userTransaction == null)
            {
                throw new IllegalStateException("cannot rollback UserTransaction in CMT environment");
            }

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
                if (this.userTransaction != null)
                {
                    this.userTransaction.setRollbackOnly();
                }
                else
                {
                    this.transactionSynchronizationRegistry.setRollbackOnly();
                }

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
                return this.getStatus() == Status.STATUS_MARKED_ROLLBACK;
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
                return this.getStatus() != Status.STATUS_NO_TRANSACTION &&
                        this.getStatus() != Status.STATUS_UNKNOWN; //TODO re-visit it
            }
            catch (SystemException e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }

        protected boolean isTransactionAllowedToRollback() throws SystemException
        {
            //if the following gets changed, it needs to be tested with different constellations
            //(normal exception, timeout,...) as well as servers
            return this.getStatus() != Status.STATUS_COMMITTED &&
                    this.getStatus() != Status.STATUS_NO_TRANSACTION &&
                    this.getStatus() != Status.STATUS_UNKNOWN;
        }

        protected boolean isTransactionReadyToCommit() throws SystemException
        {
            return this.getStatus() == Status.STATUS_ACTIVE ||
                    this.getStatus() == Status.STATUS_PREPARING ||
                    this.getStatus() == Status.STATUS_PREPARED;
        }

        protected int getStatus() throws SystemException
        {
            if (this.userTransaction != null)
            {
                return this.userTransaction.getStatus();
            }
            else
            {
                return this.transactionSynchronizationRegistry.getTransactionStatus();
            }
        }
    }
}
