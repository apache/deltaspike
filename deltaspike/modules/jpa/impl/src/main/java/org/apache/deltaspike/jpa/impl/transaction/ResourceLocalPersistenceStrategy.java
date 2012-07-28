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


import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.jpa.api.Transactional;
import org.apache.deltaspike.jpa.impl.transaction.context.EntityManagerEntry;
import org.apache.deltaspike.jpa.impl.transaction.context.TransactionBeanStorage;
import org.apache.deltaspike.jpa.spi.PersistenceStrategy;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Default implementation of our plugable PersistenceStrategy.
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
 * <p>If you like to implement your own PersistenceStrategy, then use the
 * standard CDI &#064;Alternative mechanism.</p>
 */
@Dependent
public class ResourceLocalPersistenceStrategy implements PersistenceStrategy
{
    private static final long serialVersionUID = -1432802805095533499L;

    private static final Logger LOGGER = Logger.getLogger(ResourceLocalPersistenceStrategy.class.getName());

    @Inject
    private BeanManager beanManager;

    @Inject
    private TransactionBeanStorage transactionBeanStorage;

    @Inject
    private PersistenceStrategyHelper persistenceHelper;

    public Object execute(InvocationContext invocationContext) throws Exception
    {
        Transactional transactionalAnnotation = persistenceHelper.extractTransactionalAnnotation(invocationContext);

        // all the configured qualifier keys
        Set<Class<? extends Annotation>> emQualifiers = persistenceHelper.resolveEntityManagerQualifiers(
                    transactionalAnnotation, invocationContext.getTarget().getClass());

        boolean isOutermostInterceptor = transactionBeanStorage.isEmpty();

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
                    transaction.begin();
                }

                //don't move it before EntityTransaction#begin() and invoke it in any case
                beforeProceed(entityManagerEntry);
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
            // this is a 'JTA for poor men' only, and will not guaranty
            // commit stability over various databases!
            if (isOutermostInterceptor)
            {
                // only commit all transactions if we didn't rollback
                // them already
                if (firstException == null)
                {
                    Set<EntityManagerEntry> entityManagerEntryList =
                        transactionBeanStorage.getUsedEntityManagerEntries();

                    boolean rollbackOnly = false;
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
                                        //don't set commitFailed to true directly
                                        //(the order of the entity-managers isn't deterministic -> tests would break)
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
                                if (commitFailed || transaction.getRollbackOnly() /*last chance to check it (again)*/)
                                {
                                    transaction.rollback();
                                }
                                else
                                {
                                    transaction.commit();
                                }
                            }
                            catch (Exception e)
                            {
                                firstException = e;
                                commitFailed = true;
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
                //noinspection ThrowFromFinallyBlock
                throw firstException;
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

    protected void beforeProceed(EntityManagerEntry entityManagerEntry)
    {
        //override if needed
    }

    private EntityManager resolveEntityManagerForQualifier(Class<? extends Annotation> emQualifier)
    {
        Bean<EntityManager> entityManagerBean = resolveEntityManagerBean(emQualifier);

        if (entityManagerBean == null)
        {
            return null;
        }

        return (EntityManager) beanManager.getReference(entityManagerBean, EntityManager.class,
                beanManager.createCreationalContext(entityManagerBean));
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
        //override if needed
    }

    protected Bean<EntityManager> resolveEntityManagerBean(Class<? extends Annotation> qualifierClass)
    {
        Set<Bean<?>> entityManagerBeans = beanManager.getBeans(EntityManager.class, new AnyLiteral());
        if (entityManagerBeans == null)
        {
            entityManagerBeans = new HashSet<Bean<?>>();
        }

        for (Bean<?> currentEntityManagerBean : entityManagerBeans)
        {
            Set<Annotation> foundQualifierAnnotations = currentEntityManagerBean.getQualifiers();

            for (Annotation currentQualifierAnnotation : foundQualifierAnnotations)
            {
                if (currentQualifierAnnotation.annotationType().equals(qualifierClass))
                {
                    return (Bean<EntityManager>) currentEntityManagerBean;
                }
            }
        }
        return null;
    }
}
