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
package org.apache.deltaspike.jpa.impl.transaction.context;

import org.apache.deltaspike.core.api.projectstage.TestStage;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.jpa.impl.transaction.TransactionBeanStorageCleanupTestEvent;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.Typed;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>This bean stores information about
 * &#064;{@link org.apache.deltaspike.jpa.api.TransactionScoped}
 * contextual instances, their {@link javax.enterprise.context.spi.CreationalContext} etc.</p>
 * <p/>
 * <p>We use a RequestScoped bean because this way we don't need to take
 * care about cleaning up any ThreadLocals ourselves. This also makes sure that
 * we subsequently destroy any left over TransactionScoped beans (which should not happen,
 * but who knows). We also don't need to do any fancy synchronization stuff since
 * we are sure that we are always in the same Thread.</p>
 */
@Typed()
public class TransactionBeanStorage
{
    private static final Logger LOGGER = Logger.getLogger(TransactionBeanStorage.class.getName());

    private static ThreadLocal<TransactionBeanStorage> currentStorage = new ThreadLocal<TransactionBeanStorage>();

    /**
     * This is the actual bean storage.
     * The structure is:
     * <ol>
     * <li>transactioKey identifies the 'database qualifier'</li>
     * <li>transactionKey -> Stack: we need the Stack because of REQUIRES_NEW, etc</li>
     * <li>top Element in the Stack -> Context beans for the transactionKey</li>
     * </ol>
     */
    private Map<String, Map<Contextual, TransactionBeanEntry>> storedTransactionContexts =
            new HashMap<String, Map<Contextual, TransactionBeanEntry>>();

    private List<Map<Contextual, TransactionBeanEntry>> activeTransactionContextList;

    private List<String> activeTransactionKeyList = new ArrayList<String>();

    private boolean isTestProjectStage;

    private TransactionBeanStorage()
    {
        this.isTestProjectStage = TestStage.class.isAssignableFrom(
            ProjectStageProducer.getInstance().getProjectStage().getClass());
    }

    /**
     * @return the storage for the current thread if there is one - null otherwise
     */
    public static TransactionBeanStorage getStorage()
    {
        return currentStorage.get();
    }

    /**
     * Creates a new storage for the current thread
     *
     * @return the storage which was associated with the thread before - null if there was no storage
     */
    public static TransactionBeanStorage activateNewStorage()
    {
        TransactionBeanStorage previousStorage = currentStorage.get();
        currentStorage.set(new TransactionBeanStorage());
        return previousStorage;
    }

    /**
     * Removes the current storage
     */
    public static void resetStorage()
    {
        TransactionBeanStorage currentBeanStorage = currentStorage.get();

        if (currentBeanStorage != null)
        {
            currentBeanStorage.close();

            currentStorage.set(null);
            currentStorage.remove();
        }
    }

    private void close()
    {
        if (this.isTestProjectStage)
        {
            BeanManagerProvider.getInstance().getBeanManager().fireEvent(new TransactionBeanStorageCleanupTestEvent());
        }
    }

    /**
     * Start the TransactionScope with the given qualifier
     *
     * @param transactionKey
     */
    public void startTransactionScope(String transactionKey)
    {
        if (LOGGER.isLoggable(Level.FINER))
        {
            LOGGER.finer("starting TransactionScope " + transactionKey);
        }

        Map<Contextual, TransactionBeanEntry> transactionBeanEntryMap = storedTransactionContexts.get(transactionKey);

        if (transactionBeanEntryMap == null)
        {
            transactionBeanEntryMap = new HashMap<Contextual, TransactionBeanEntry>();
            storedTransactionContexts.put(transactionKey, transactionBeanEntryMap);
        }
    }

    /**
     * Activate the TransactionScope with the given qualifier.
     * This is needed if a subsequently invoked &#064;Transactional
     * method will switch to another persistence unit.
     * This method must also be invoked when the transaction just got started
     * with {@link #startTransactionScope(String)}.
     */
    public void activateTransactionScope(List<String> transactionKeyList)
    {
        //TODO remove it after a review
        /*
        if (transactionKeyList != null && transactionKeyList.isEmpty())
        {
            transactionKeyList.add(Default.class.getName()); //needed for the transaction test helper
        }
        */

        if (LOGGER.isLoggable(Level.FINER))
        {
            if (transactionKeyList != null && LOGGER.isLoggable(Level.FINER))
            {
                for (String transactionKey : transactionKeyList)
                {
                    LOGGER.finer("activating TransactionScope " + transactionKey);
                }
            }
        }

        //can be null on the topmost stack-layer
        if (transactionKeyList == null && this.activeTransactionKeyList == null)
        {
            return;
        }

        if (transactionKeyList == null)
        {
            transactionKeyList = this.activeTransactionKeyList;
        }

        if (activeTransactionContextList == null)
        {
            activeTransactionContextList = new ArrayList<Map<Contextual, TransactionBeanEntry>>();
        }

        for (String transactionKey : transactionKeyList)
        {
            Map<Contextual, TransactionBeanEntry> transactionBeanEntryMap =
                    this.storedTransactionContexts.get(transactionKey);

            if (transactionBeanEntryMap == null)
            {
                throw new IllegalStateException("Cannot activate TransactionScope with key " + transactionKey);
            }

            if (!this.activeTransactionContextList.contains(transactionBeanEntryMap))
            {
                this.activeTransactionContextList.add(transactionBeanEntryMap);
            }
        }

        this.activeTransactionKeyList.addAll(transactionKeyList);
    }

    public List<String> getActiveTransactionKeyList()
    {
        return Collections.unmodifiableList(this.activeTransactionKeyList);
    }

    /**
     * This will destroy all stored transaction contexts.
     */
    public void endAllTransactionScopes()
    {
        if (LOGGER.isLoggable(Level.FINER))
        {
            LOGGER.finer("destroying all TransactionScopes");
        }

        for (Map<Contextual, TransactionBeanEntry> beans : this.storedTransactionContexts.values())
        {
            destroyBeans(beans);
        }

        // we also need to clean our active context info
        storedTransactionContexts.clear();
        activeTransactionContextList.clear();
        activeTransactionKeyList.clear();
    }


    /**
     * @return the Map which represents the currently active Context content.
     */
    public List<Map<Contextual, TransactionBeanEntry>> getActiveTransactionContextList()
    {
        return Collections.unmodifiableList(this.activeTransactionContextList);
    }

    /**
     * Properly destroy all the given beans.
     *
     * @param activeBeans
     */
    private void destroyBeans(Map<Contextual, TransactionBeanEntry> activeBeans)
    {
        for (TransactionBeanEntry beanBag : activeBeans.values())
        {
            beanBag.getBean().destroy(beanBag.getContextualInstance(), beanBag.getCreationalContext());
        }
    }

    public void storeTransactionBeanEntry(String transactionKey, TransactionBeanEntry transactionBeanEntry)
    {
        if (!this.activeTransactionKeyList.contains(transactionKey))
        {
            throw new IllegalStateException("Transaction for " + transactionKey + " is not active.");
        }

        Map<Contextual, TransactionBeanEntry> storedTransactionContext =
            this.storedTransactionContexts.get(transactionKey);

        if (storedTransactionContext == null)
        {
            storedTransactionContext = new HashMap<Contextual, TransactionBeanEntry>();
            this.storedTransactionContexts.put(transactionKey, storedTransactionContext);
        }

        storedTransactionContext.put(transactionBeanEntry.getBean(), transactionBeanEntry);
    }
}

