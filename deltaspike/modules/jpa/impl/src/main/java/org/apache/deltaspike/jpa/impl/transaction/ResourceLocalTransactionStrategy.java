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


import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.deltaspike.core.util.ProxyUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.apache.deltaspike.jpa.impl.entitymanager.EntityManagerMetadata;
import org.apache.deltaspike.jpa.impl.transaction.context.EntityManagerEntry;
import org.apache.deltaspike.jpa.impl.transaction.context.TransactionBeanStorage;
import org.apache.deltaspike.jpa.spi.entitymanager.ActiveEntityManagerHolder;
import org.apache.deltaspike.jpa.spi.entitymanager.QualifierBackedEntityManagerResolver;
import org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy;

/**
 * <p>Default implementation of our plugable TransactionStrategy.
 * It supports nested Transactions with the MANDATORY behaviour.</p>
 *
 * <p>The outermost &#064;Transactional interceptor for the given
 * {@link javax.inject.Qualifier} will open an {@link javax.persistence.EntityTransaction}
 * and the outermost &#064;Transactional interceptor for <b>all</b>
 * EntityManagers will flush and subsequently close all open transactions.</p>
 *
 * <p>If an Exception occurs in flushing the EntityManagers or any other Exception
 * gets thrown inside the intercepted method chain and <i>not</i> gets catched
 * until the outermost &#064;Transactional interceptor gets reached, then all
 * open transactions will get rollbacked.</p>
 *
 * <p>If you like to implement your own TransactionStrategy, then use the
 * standard CDI &#064;Alternative mechanism.</p>
 */
@Dependent
public class ResourceLocalTransactionStrategy implements TransactionStrategy
{
    private static final long serialVersionUID = -1432802805095533499L;

    private static final Logger LOGGER = Logger.getLogger(ResourceLocalTransactionStrategy.class.getName());

    @Inject
    private BeanManager beanManager;

    @Inject
    private TransactionStrategyHelper transactionHelper;

    @Inject
    private ActiveEntityManagerHolder emHolder;

    @Override
    public Object execute(InvocationContext invocationContext) throws Exception
    {
        EntityManagerMetadata metadata = transactionHelper.createEntityManagerMetadata(invocationContext);
        Transactional transactionalAnnotation = transactionHelper.extractTransactionalAnnotation(invocationContext);

        Class targetClass = ProxyUtils.getUnproxiedClass(invocationContext.getTarget().getClass()); //see DELTASPIKE-517

        // all the configured qualifier keys
        Set<Class<? extends Annotation>> emQualifiers = emHolder.isSet() ?
                new HashSet<Class<? extends Annotation>>(Arrays.asList(Default.class)) :
                transactionHelper.resolveEntityManagerQualifiers(metadata, targetClass);

        TransactionBeanStorage transactionBeanStorage = TransactionBeanStorage.getInstance();

        boolean isOutermostInterceptor = transactionBeanStorage.isEmpty();
        boolean startedTransaction = false;

        if (isOutermostInterceptor)
        {
            // a new Context needs to get started
            transactionBeanStorage.startTransactionScope();
        }

        // the 'layer' of the transactional invocation, aka the refCounter
        @SuppressWarnings("UnusedDeclaration")
        int transactionLayer = transactionBeanStorage.incrementRefCounter();

        Exception firstException = null;

        try
        {
            for (Class<? extends Annotation> emQualifier : emQualifiers)
            {
                EntityManager entityManager = resolveEntityManagerForQualifier(emQualifier);
                EntityManagerEntry entityManagerEntry = createEntityManagerEntry(entityManager, emQualifier);
                transactionBeanStorage.storeUsedEntityManager(entityManagerEntry);

                EntityTransaction transaction = getTransaction(entityManagerEntry);

                if (!transaction.isActive())
                {
                    beforeBegin(invocationContext, entityManagerEntry, transaction);
                    transaction.begin();
                    startedTransaction = true;
                }

                //don't move it before EntityTransaction#begin() and invoke it in any case
                beforeProceed(invocationContext, entityManagerEntry, transaction);
            }

            return invocationContext.proceed();
        }
        catch (Exception e)
        {
            firstException = e;

            // we only cleanup and rollback all open transactions in the outermost interceptor!
            // this way, we allow inner functions to catch and handle exceptions properly.
            if (isOutermostInterceptor)
            {
                Set<EntityManagerEntry> entityManagerEntryList =
                    transactionBeanStorage.getUsedEntityManagerEntries();

                if (startedTransaction)
                {
                    // We only commit transactions we opened ourselfs.
                    // If the transaction got opened outside of our interceptor chain
                    // we must not handle it.
                    // This e.g. happens if a Stateless EJB invokes a Transactional CDI bean
                    // which uses the BeanManagedUserTransactionStrategy.

                    rollbackAllTransactions(entityManagerEntryList);
                }

                // drop all EntityManagers from the request-context cache
                transactionBeanStorage.cleanUsedEntityManagers();
            }

            // give any extensions a chance to supply a better error message
            e = prepareException(e);

            // rethrow the exception
            throw e;
        }
        finally
        {
            // will get set if we got an Exception while committing
            // in this case, we rollback all later transactions too.
            boolean commitFailed = false;

            // commit all open transactions in the outermost interceptor!
            // For Resource-local this is a 'JTA for poor men' only, and will not guaranty
            // commit stability over various databases!
            // In case of JTA we will just commit the UserTransaction.
            if (isOutermostInterceptor)
            {
                if (startedTransaction)
                {
                    // We only commit transactions we opened ourselfs.
                    // If the transaction got opened outside of our interceptor chain
                    // we must not handle it.
                    // This e.g. happens if a Stateless EJB invokes a Transactional CDI bean
                    // which uses the BeanManagedUserTransactionStrategy.

                    if (firstException == null)
                    {
                        // only commit all transactions if we didn't rollback
                        // them already
                        Set<EntityManagerEntry> entityManagerEntryList =
                            transactionBeanStorage.getUsedEntityManagerEntries();

                        boolean rollbackOnly = metadata.isReadOnly() || isRollbackOnly(transactionalAnnotation);

                        if (!rollbackOnly && entityManagerEntryList.size() > 1)
                        {
                            // but first try to flush all the transactions and write the updates to the database
                            for (EntityManagerEntry currentEntityManagerEntry : entityManagerEntryList)
                            {
                                EntityTransaction transaction = getTransaction(currentEntityManagerEntry);
                                if (transaction != null && transaction.isActive())
                                {
                                    try
                                    {
                                        if (!commitFailed)
                                        {
                                            currentEntityManagerEntry.getEntityManager().flush();

                                            if (!rollbackOnly && transaction.getRollbackOnly())
                                            {
                                                // don't set commitFailed to true directly
                                                // (the order of the entity-managers isn't deterministic
                                                //  -> tests would break)
                                                rollbackOnly = true;
                                            }
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        firstException = e;
                                        commitFailed = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (rollbackOnly)
                        {
                            commitFailed = true;
                        }

                        // and now either commit or rollback all transactions
                        for (EntityManagerEntry currentEntityManagerEntry : entityManagerEntryList)
                        {
                            EntityTransaction transaction = getTransaction(currentEntityManagerEntry);
                            if (transaction != null && transaction.isActive())
                            {
                                try
                                {
                                    // last chance to check it (again)
                                    if (commitFailed || transaction.getRollbackOnly())
                                    {
                                        beforeRollback(invocationContext, currentEntityManagerEntry, transaction);
                                        transaction.rollback();
                                    }
                                    else
                                    {
                                        beforeCommit(invocationContext, currentEntityManagerEntry, transaction);
                                        transaction.commit();
                                    }
                                }
                                catch (Exception e)
                                {
                                    firstException = e;
                                    commitFailed = true;
                                }
                                finally
                                {
                                    afterProceed(invocationContext,currentEntityManagerEntry, firstException);
                                }
                            }
                        }
                    }
                }
                // and now we close the open transaction scope
                transactionBeanStorage.endTransactionScope();
                onCloseTransactionScope();
            }

            transactionBeanStorage.decrementRefCounter();

            if (commitFailed && firstException != null /*null if just #getRollbackOnly is true*/)
            {
                throwException(firstException);
            }
        }
    }

    protected void beforeBegin(InvocationContext invocationContext,
                               EntityManagerEntry entityManagerEntry,
                               EntityTransaction transaction)
    {
        //override if needed
    }

    protected void beforeProceed(InvocationContext invocationContext,
                                 EntityManagerEntry entityManagerEntry,
                                 EntityTransaction transaction)
    {
        //override if needed
    }

    protected void beforeCommit(InvocationContext invocationContext,
                                EntityManagerEntry entityManagerEntry,
                                EntityTransaction transaction)
    {
        //override if needed
    }

    protected void beforeRollback(InvocationContext invocationContext,
                                  EntityManagerEntry entityManagerEntry,
                                  EntityTransaction transaction)
    {
        //override if needed
    }

    /**
     * @param invocationContext current invocation-context
     * @param entityManagerEntry current entity-manager entry
     * @param exception the exception which occurred or null if everything went fine
     */
    protected void afterProceed(InvocationContext invocationContext,
                                EntityManagerEntry entityManagerEntry,
                                Exception exception)
    {
        //override if needed
    }

    protected void throwException(Exception exception) throws Exception
    {
        //override if needed
        throw exception;
    }

    //allows to use a custom tx-controller in a custom strategy
    @Deprecated // instead, configure read only on the method/class
    protected boolean isRollbackOnly(Transactional transactionalAnnotation)
    {
        return transactionalAnnotation != null && transactionalAnnotation.readOnly();
    }

    private void rollbackAllTransactions(Set<EntityManagerEntry> entityManagerEntryList)
    {
        for (EntityManagerEntry currentEntityManagerEntry : entityManagerEntryList)
        {
            EntityTransaction transaction = getTransaction(currentEntityManagerEntry);
            if (transaction != null && transaction.isActive())
            {
                try
                {
                    transaction.rollback();
                }
                catch (Exception eRollback)
                {
                    if (LOGGER.isLoggable(Level.SEVERE))
                    {
                        LOGGER.log(Level.SEVERE,
                                "Got additional Exception while subsequently " +
                                "rolling back other SQL transactions", eRollback);
                    }
                }
            }
        }
    }

    protected EntityManagerEntry createEntityManagerEntry(
        EntityManager entityManager, Class<? extends Annotation> qualifier)
    {
        return new EntityManagerEntry(entityManager, qualifier);
    }

    /**
     *
     * @param entityManagerEntry entry of the current entity-manager
     * @return per default the {@link EntityTransaction} of the given {@link EntityManager}.
     * A subclass can also return an adapter e.g. for an UserTransaction
     */
    protected EntityTransaction getTransaction(EntityManagerEntry entityManagerEntry)
    {
        return entityManagerEntry.getEntityManager().getTransaction();
    }

    private EntityManager resolveEntityManagerForQualifier(Class<? extends Annotation> emQualifier)
    {
        if (emHolder.isSet())
        {
            return emHolder.get();
        }
        return new QualifierBackedEntityManagerResolver(beanManager, emQualifier).resolveEntityManager();
    }

    /**
     * This method might get overridden in subclasses to supply better error messages.
     * This is useful if e.g. a JPA provider only provides a stubborn Exception for
     * their ConstraintValidationExceptions.
     * @return the wrapped or unwrapped Exception
     */
    protected Exception prepareException(Exception e)
    {
        return e;
    }

    protected void onCloseTransactionScope()
    {
        TransactionBeanStorage.close();
    }
}
