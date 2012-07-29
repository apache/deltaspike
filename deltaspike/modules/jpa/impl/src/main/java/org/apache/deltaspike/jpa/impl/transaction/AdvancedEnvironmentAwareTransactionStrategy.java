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

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.jpa.impl.transaction.context.EntityManagerEntry;
import org.apache.deltaspike.jpa.impl.transaction.context.JtaEntityManagerEntry;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This alternative {@link org.apache.deltaspike.jpa.spi.TransactionStrategy} uses auto-detection and
 * can be used if different environments (dev., prod.,...) should use different transaction-types.
 *
 * This implementation uses a different approach for the auto-detection which can be used for environments
 * (or producer-constellations) which allow a mixed usage of JTA and RESOURCE_LOCAL.
 * (Within a transactional call it isn't possible to mix different transaction-types.)
 *
 * @see EnvironmentAwareTransactionStrategy
 */
@Dependent
@Alternative
@SuppressWarnings("UnusedDeclaration")
public class AdvancedEnvironmentAwareTransactionStrategy extends EnvironmentAwareTransactionStrategy
{
    private static final long serialVersionUID = -4432802805095533499L;

    private static final Logger LOGGER =
        Logger.getLogger(AdvancedEnvironmentAwareTransactionStrategy.class.getName());

    private static ThreadLocal<Boolean> isJtaModeDetected = new ThreadLocal<Boolean>();

    @Inject
    private ProjectStage projectStage;

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
            isTransactionTypeJta = isJtaModeDetected.get();
        }

        if (!isTransactionTypeJta && this.projectStage == ProjectStage.Development)
        {
            LOGGER.log(Level.INFO, getClass().getName() + " is active and " +
                    "RESOURCE_LOCAL is configured for persistent-unit-transaction-type in the persistence.xml. " +
                    "That's valid in case of different transaction-types for different environments " +
                    "e.g. for development and production. " +
                    "Please check your setup - if that isn't the intended use-case, " +
                    "you could also use the default strategy " + ResourceLocalTransactionStrategy.class.getName());
        }

        return new JtaEntityManagerEntry(entityManager, qualifier, isTransactionTypeJta);
    }

    @Override
    protected void onCloseTransactionScope()
    {
        super.onCloseTransactionScope();
        isJtaModeDetected.set(null);
        isJtaModeDetected.remove();
    }
}
