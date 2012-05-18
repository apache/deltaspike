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

import org.apache.deltaspike.jpa.api.Transactional;
import org.apache.deltaspike.jpa.impl.EntityManagerRef;
import org.apache.deltaspike.jpa.impl.PersistenceHelper;
import org.apache.deltaspike.jpa.impl.transaction.context.TransactionBeanStorage;
import org.apache.deltaspike.jpa.spi.PersistenceStrategy;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Default implementation of our pluggable PersistenceStrategy.
 * It supports nested Transactions with the MANDATORY behaviour.</p>
 * <p/>
 * <p>The outermost &#064;Transactional interceptor for the given
 * {@link javax.inject.Qualifier} will open an {@link javax.persistence.EntityTransaction}
 * and the outermost &#064;Transactional interceptor for <b>all</b>
 * EntityManagers will flush and subsequently close all open transactions.</p>
 * <p/>
 * <p>If an Exception occurs in flushing the EntityManagers or any other Exception
 * gets thrown inside the intercepted method chain and <i>not</i> gets catched
 * until the outermost &#064;Transactional interceptor gets reached, then all
 * open transactions will get rollbacked.</p>
 * <p/>
 * <p>If you like to implement your own PersistenceStrategy, then use the
 * standard CDI &#064;Alternative mechanism.</p>
 */
@Dependent
public class TransactionalInterceptorStrategy implements PersistenceStrategy
{
    private static final long serialVersionUID = 2433371956913151976L;

    private static final Logger LOGGER = Logger.getLogger(TransactionalInterceptorStrategy.class.getName());

    /**
     * key=qualifier name, value= reference counter
     */
    private static transient ThreadLocal<InternalTransactionContext> transactionContext =
            new ThreadLocal<InternalTransactionContext>();

    @Inject
    private BeanManager beanManager;

    public Object execute(InvocationContext invocationContext) throws Exception
    {
        Transactional transactionalAnnotation = extractTransactionalAnnotation(invocationContext);

        InternalTransactionContext currentTransactionContext =
                getOrCreateTransactionContext(transactionalAnnotation, invocationContext.getTarget());

        List<String> transactionKeys = getTransactionKeys(currentTransactionContext);

        if (TransactionBeanStorage.getStorage() == null)
        {
            TransactionBeanStorage.activateNewStorage();
        }

        for (String transactionKey : transactionKeys)
        {
            TransactionBeanStorage.getStorage().startTransactionScope(transactionKey);
        }

        List<String> previousTransactionKeys = null;

        if (transactionKeys != null && !transactionKeys.isEmpty())
        {
            TransactionBeanStorage.getStorage().activateTransactionScope(transactionKeys);
        }

        beginOrJoinTransactionsAndEnter(currentTransactionContext);

        // used to store any exception we get from the services
        Exception firstException = null;

        try
        {
            return invocationContext.proceed();
        }
        catch (Exception e)
        {
            firstException = e;

            leave(currentTransactionContext);
            // we only cleanup and rollback all open transactions in the outermost interceptor!
            // this way, we allow inner functions to catch and handle exceptions properly.
            if (isOutermostInterceptor(currentTransactionContext))
            {
                for (TransactionMetaDataEntry transactionMetaDataEntry :
                        currentTransactionContext.getTransactionMetaDataEntries())
                {
                    try
                    {
                        EntityTransaction transaction = transactionMetaDataEntry.getEntityManager().getTransaction();

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
                    catch (IllegalStateException e2)
                    {
                        //just happens if the setup is wrong -> we can't do a proper cleanup
                        //but we have to continue to cleanup the scope
                    }
                }
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

            // only commit all transactions if we didn't rollback them already
            if (firstException == null)
            {
                leave(currentTransactionContext);

                // commit all open transactions in the outermost interceptor!
                // this is a 'JTA for poor men' only, and will not guaranty
                // commit stability over various databases!
                if (isOutermostInterceptor(currentTransactionContext))
                {
                    EntityTransaction transaction;
                    EntityManager entityManager;
                    // but first try to flush all the transactions and write the updates to the database
                    for (TransactionMetaDataEntry transactionMetaDataEntry :
                            currentTransactionContext.getTransactionMetaDataEntries())
                    {
                        entityManager = transactionMetaDataEntry.getEntityManager();
                        transaction = entityManager.getTransaction();

                        if (transaction != null && transaction.isActive())
                        {
                            try
                            {
                                if (!commitFailed)
                                {
                                    entityManager.flush();
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

                    // and now either commit or rollback all transactions
                    for (TransactionMetaDataEntry transactionMetaDataEntry :
                            currentTransactionContext.getTransactionMetaDataEntries())
                    {
                        entityManager = transactionMetaDataEntry.getEntityManager();
                        transaction = entityManager.getTransaction();

                        if (transaction != null && transaction.isActive())
                        {
                            try
                            {
                                if (!commitFailed)
                                {
                                    transaction.commit(); //shouldn't fail since the transaction was flushed already
                                }
                                else
                                {
                                    transaction.rollback();
                                }
                            }
                            catch (Exception e)
                            {
                                firstException = e;
                                commitFailed = true;
                            }
                        }
                    }

                    // and now we close all open transaction-scopes and reset the storage
                    TransactionBeanStorage.getStorage().endAllTransactionScopes();
                    TransactionBeanStorage.resetStorage();
                }
                else
                {
                    // we are NOT the outermost TransactionInterceptor
                    // so we have to re-activate the previous transaction
                    if (previousTransactionKeys != null && !previousTransactionKeys.isEmpty())
                    {
                        TransactionBeanStorage.getStorage().activateTransactionScope(previousTransactionKeys);
                    }
                }
            }

            cleanup(currentTransactionContext);

            if (commitFailed)
            {
                //noinspection ThrowFromFinallyBlock
                throw firstException;
            }
        }
    }

    private List<String> getTransactionKeys(InternalTransactionContext currentTransactionContext)
    {
        List<String> transactionKeys = new ArrayList<String>();

        for (TransactionMetaDataEntry transactionMetaDataEntry :
                currentTransactionContext.getTransactionMetaDataEntries())
        {
            if (transactionMetaDataEntry.getMethodCallDepth() == 0)
            {
                transactionKeys.add(transactionMetaDataEntry.getId());
            }
        }

        return transactionKeys;
    }

    private void removeTransactionContext()
    {
        transactionContext.set(null);
        transactionContext.remove();
    }

    private void startTransactionStorage(String transactionKey)
    {
        if (TransactionBeanStorage.getStorage() == null)
        {
            TransactionBeanStorage.activateNewStorage();
        }

        TransactionBeanStorage.getStorage().startTransactionScope(transactionKey);
    }

    private void beginOrJoinTransactionsAndEnter(InternalTransactionContext transactionContext)
    {
        for (TransactionMetaDataEntry transactionMetaDataEntry : transactionContext.getTransactionMetaDataEntries())
        {
            if (transactionMetaDataEntry.getMethodCallDepth() == 0)
            {
                startTransactionStorage(transactionMetaDataEntry.getId());

                beginTransaction(transactionMetaDataEntry);
            }
            transactionMetaDataEntry.enterNewMethodLevel();
        }
    }

    private void beginTransaction(TransactionMetaDataEntry transactionMetaDataEntry)
    {
        EntityManager entityManager = transactionMetaDataEntry.getEntityManager();

        EntityTransaction transaction = entityManager.getTransaction();

        if (!transaction.isActive())
        {
            transaction.begin();
            transactionMetaDataEntry.markLevel();
        }
    }

    /**
     * This method might get overridden in subclasses to supply better error messages.
     * This is useful if e.g. a JPA provider only provides a stubborn Exception for
     * their ConstraintValidationExceptions.
     */
    protected Exception prepareException(Exception e)
    {
        return e;
    }

    protected Transactional extractTransactionalAnnotation(InvocationContext context)
    {
        Transactional transactionalAnnotation = context.getMethod().getAnnotation(Transactional.class);

        if (transactionalAnnotation == null)
        {
            transactionalAnnotation = context.getTarget().getClass().getAnnotation(Transactional.class);
        }

        //check class stereotypes
        if (transactionalAnnotation == null)
        {
            for (Annotation annotation : context.getTarget().getClass().getAnnotations())
            {
                if (this.beanManager.isQualifier(annotation.annotationType()))
                {
                    for (Annotation metaAnnotation : annotation.annotationType().getAnnotations())
                    {
                        if (Transactional.class.isAssignableFrom(metaAnnotation.annotationType()))
                        {
                            return (Transactional) metaAnnotation;
                        }
                    }
                }
            }
        }

        return transactionalAnnotation;
    }

    private InternalTransactionContext getOrCreateTransactionContext(Transactional transactionalAnnotation,
                                                                     Object target)
    {
        InternalTransactionContext currentTransactionContext = transactionContext.get();

        if (currentTransactionContext == null)
        {
            currentTransactionContext = new InternalTransactionContext(beanManager);
            transactionContext.set(currentTransactionContext);
        }

        if (transactionalAnnotation == null)
        {
            //TODO check if we still need it
            currentTransactionContext.addTransactionMetaDataEntry(Default.class);
        }
        else if (!Any.class.isAssignableFrom(transactionalAnnotation.qualifier()[0]))
        {
            for (Class<? extends Annotation> qualifier : transactionalAnnotation.qualifier())
            {
                currentTransactionContext.addTransactionMetaDataEntry(qualifier);
            }
        }
        else
        {
            findAndAddInjectedEntityManagers(target, currentTransactionContext);
        }

        //TODO remove it if we don't use Transactional#autoDetection
        /*
        if (transactionalAnnotation == null)
        {
            currentTransactionContext.addTransactionMetaDataEntry(Default.class);
        }
        else if (!transactionalAnnotation.autoDetection())
        {
            for (Class<? extends Annotation> qualifier : transactionalAnnotation.qualifier())
            {
                currentTransactionContext.addTransactionMetaDataEntry(qualifier);
            }
        }
        else
        {
            findAndAddInjectedEntityManagers(target, currentTransactionContext);
        }
        */

        return currentTransactionContext;
    }

    private void findAndAddInjectedEntityManagers(Object target, InternalTransactionContext currentTransactionContext)
    {
        List<EntityManagerRef> entityManagerRefList = PersistenceHelper.tryToFindEntityManagerReference(target);

        if (entityManagerRefList == null)
        {
            return;
        }

        for (EntityManagerRef entityManagerRef : entityManagerRefList)
        {
            currentTransactionContext.addTransactionMetaDataEntry(
                    entityManagerRef.getKey(), entityManagerRef.getEntityManager(target));
        }
    }

    private void leave(InternalTransactionContext currentTransactionContext)
    {
        for (TransactionMetaDataEntry transactionMetaDataEntry :
                currentTransactionContext.getTransactionMetaDataEntries())
        {
            transactionMetaDataEntry.leave();
        }
    }

    private boolean isOutermostInterceptor(InternalTransactionContext currentTransactionContext)
    {
        for (TransactionMetaDataEntry transactionMetaDataEntry :
                currentTransactionContext.getTransactionMetaDataEntries())
        {
            //can be < 0 if a 2nd entity-manager is used for a nested call which
            //should be committed/rolled back before the outermost method returns
            if (transactionMetaDataEntry.getMethodCallDepth() > 0)
            {
                return false;
            }
        }

        return true;
    }

    private void cleanup(InternalTransactionContext currentTransactionContext)
    {
        for (TransactionMetaDataEntry transactionMetaDataEntry :
                currentTransactionContext.getTransactionMetaDataEntries())
        {
            //can be < 0 if a 2nd entity-manager is used for a nested call which
            //should be committed/rolled back before the outermost method returns
            if (transactionMetaDataEntry.getMethodCallDepth() > 0)
            {
                return;
            }
        }

        removeTransactionContext();
    }
}
