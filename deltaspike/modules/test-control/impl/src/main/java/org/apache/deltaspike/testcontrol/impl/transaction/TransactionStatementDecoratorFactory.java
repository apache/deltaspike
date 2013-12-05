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
package org.apache.deltaspike.testcontrol.impl.transaction;

import junit.framework.AssertionFailedError;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.testcontrol.spi.junit.TestStatementDecoratorFactory;
import org.junit.After;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Not compatible with too short scopes, because they are closed too early
 */
public class TransactionStatementDecoratorFactory implements TestStatementDecoratorFactory
{
    private static final Logger LOG = Logger.getLogger(TransactionStatementDecoratorFactory.class.getName());

    static
    {
        LOG.setLevel(Level.INFO);
    }

    @Override
    public Statement createBeforeStatement(Statement originalStatement, TestClass testClass, Object target)
    {
        return originalStatement;
    }

    @Override
    public Statement createAfterStatement(Statement originalStatement, TestClass testClass, Object target)
    {
        final List<FrameworkMethod> afters = testClass.getAnnotatedMethods(After.class);
        return new TransactionAwareRunAfters(originalStatement, afters, target);
    }

    @Override
    public int getOrdinal()
    {
        return 1000; //default in ds
    }

    //see org.junit.internal.runners.statements.RunAfters
    private class TransactionAwareRunAfters extends Statement
    {
        private final Statement wrapped;
        private final List<FrameworkMethod> afters;
        private final Object target;

        public TransactionAwareRunAfters(Statement wrapped, List<FrameworkMethod> afters, Object target)
        {
            this.wrapped = wrapped;
            this.afters = afters;
            this.target = target;
        }

        @Override
        public void evaluate() throws Throwable
        {
            List<Throwable> result = new ArrayList<Throwable>();

            try
            {
                this.wrapped.evaluate();
            }
            catch (Throwable e)
            {
                result.add(performConsistencyCheck(e));
            }
            finally
            {
                Throwable t = performConsistencyCheck(null);

                if (t != null)
                {
                    result.add(t);
                }

                for (FrameworkMethod each : this.afters)
                {
                    try
                    {
                        each.invokeExplosively(this.target);
                    }
                    catch (Throwable e)
                    {
                        result.add(e);
                    }
                }
            }

            if (!result.isEmpty())
            {
                MultipleFailureException.assertEmpty(result);
            }
        }

        private Throwable performConsistencyCheck(Throwable t)
        {
            Throwable result = t;

            if (t instanceof InvocationTargetException)
            {
                result = t.getCause();
            }

            List<EntityManager> entityManagerList =
                    BeanProvider.getContextualReferences(EntityManager.class, true, false);

            for (Field field : this.target.getClass().getDeclaredFields())
            {
                if (EntityManager.class.isAssignableFrom(field.getType()))
                {
                    field.setAccessible(true);
                    try
                    {
                        entityManagerList.add((EntityManager) field.get(this.target));
                    }
                    catch (Exception e)
                    {
                        throw ExceptionUtils.throwAsRuntimeException(e);
                    }
                }
            }

            for (EntityManager entityManager : entityManagerList)
            {
                if (entityManager.getTransaction().isActive())
                {
                    if (t instanceof AssertionFailedError)
                    {
                        LOG.severe("assert failed within a transaction");
                    }
                    LOG.severe("start manual rollback");

                    //force rollback - otherwise there can be side-effects in other tests or cleanup-code
                    // (e.g. 'TRUNCATE SCHEMA' would fail)
                    entityManager.getTransaction().rollback();

                    if (result == null)
                    {
                        result = new IllegalStateException("open transaction found");
                    }
                }
            }

            return result;
        }
    }
}
