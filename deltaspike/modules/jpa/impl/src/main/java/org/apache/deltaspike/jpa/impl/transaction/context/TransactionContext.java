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


import org.apache.deltaspike.jpa.api.TransactionScoped;
import org.apache.deltaspike.jpa.api.Transactional;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CDI Context for managing &#064;{@link TransactionScoped} contextual instances.
 */
@Typed()
public class TransactionContext implements Context
{
    public <T> T get(Contextual<T> component)
    {
        List<Map<Contextual, TransactionBeanEntry>> transactionBeanEntryMaps = getTransactionBeanEntryMaps();

        if (transactionBeanEntryMaps == null)
        {
            return null;
        }

        TransactionBeanEntry transactionBeanEntry = null;

        for (Map<Contextual, TransactionBeanEntry> transactionBeanEntryMap : transactionBeanEntryMaps)
        {
            transactionBeanEntry = transactionBeanEntryMap.get(component);

            if (transactionBeanEntry != null)
            {
                break;
            }
        }

        if (transactionBeanEntry != null)
        {
            checkTransactionBeanEntry(transactionBeanEntry);
            return (T) transactionBeanEntry.getContextualInstance();
        }

        return null;
    }

    public <T> T get(Contextual<T> component, CreationalContext<T> creationalContext)
    {
        if (!(component instanceof Bean))
        {
            throw new IllegalStateException(Contextual.class.getName() + " is not of type " + Bean.class.getName());
        }

        Set<Annotation> qualifiers = ((Bean)component).getQualifiers();
        Set<Annotation> transactionKeys = new HashSet<Annotation>();

        for (Annotation currentQualifier : qualifiers)
        {
            if (Any.class.isAssignableFrom(currentQualifier.annotationType()))
            {
                continue;
            }
            if (Named.class.isAssignableFrom(currentQualifier.annotationType()))
            {
                continue;
            }

            //TODO since we just support a simple qualifier as key, we can exclude all other qualifiers

            transactionKeys.add(currentQualifier);
        }
        
        if (transactionKeys.size() != 1)
        {
            throw new IllegalStateException(transactionKeys.size() + " qualifiers found at " + component.toString() +
                " only one is allowed!");
        }
        
        String transactionKey = transactionKeys.iterator().next().annotationType().getName();
        
        if (TransactionBeanStorage.getStorage().getActiveTransactionContextList() == null)
        {
            TransactionBeanStorage.activateNewStorage();
        }

        List<Map<Contextual, TransactionBeanEntry>> activeTransactionBeanEntryMaps = getTransactionBeanEntryMaps();

        if (activeTransactionBeanEntryMaps == null)
        {
            throw new ContextNotActiveException("Not accessed within a transactional method - use @" +
                    Transactional.class.getName());
        }

        TransactionBeanEntry transactionBeanEntry;
        if (!activeTransactionBeanEntryMaps.isEmpty())
        {
            for (Map<Contextual, TransactionBeanEntry> currentTransactionBeanEntryMap : activeTransactionBeanEntryMaps)
            {
                transactionBeanEntry = currentTransactionBeanEntryMap.get(component);

                if (transactionBeanEntry != null)
                {
                    checkTransactionBeanEntry(transactionBeanEntry);
                    return (T) transactionBeanEntry.getContextualInstance();
                }
            }
        }

        // if it doesn't yet exist, we need to create it now!
        T instance = component.create(creationalContext);
        transactionBeanEntry = new TransactionBeanEntry(component, instance, creationalContext);

        checkTransactionBeanEntry(transactionBeanEntry);

        TransactionBeanStorage.getStorage().storeTransactionBeanEntry(transactionKey, transactionBeanEntry);

        return instance;
    }

    private void checkTransactionBeanEntry(TransactionBeanEntry<?> transactionBeanEntry)
    {
        List<String> activeTransactionKeys = TransactionBeanStorage.getStorage().getActiveTransactionKeyList();

        for (Annotation qualifier : transactionBeanEntry.getQualifiers())
        {
            if (activeTransactionKeys.contains(qualifier.annotationType().getName()))
            {
                return;
            }
        }

        throw new IllegalStateException("Transaction qualifier of the intercepted bean or method and " +
                "the injected entity-manager has to be the same. Active transaction qualifier: " +
                //TODO
                activeTransactionKeys + " qualifier/s of the entity-manager: " +
                extractQualifiers(transactionBeanEntry));
    }

    private String extractQualifiers(TransactionBeanEntry<?> transactionBeanEntry)
    {
        StringBuilder result = new StringBuilder();
        for (Annotation annotation : transactionBeanEntry.getQualifiers())
        {
            if (result.length() != 0)
            {
                result.append(";");
            }

            result.append(annotation.annotationType().getName());
        }
        return result.toString();
    }

    private List<Map<Contextual, TransactionBeanEntry>> getTransactionBeanEntryMaps()
    {
        TransactionBeanStorage transactionBeanStorage = TransactionBeanStorage.getStorage();

        if (transactionBeanStorage != null)
        {
            transactionBeanStorage.activateTransactionScope(null);
            return transactionBeanStorage.getActiveTransactionContextList();
        }
        return null;
    }

    public Class<? extends Annotation> getScope()
    {
        return TransactionScoped.class;
    }

    public boolean isActive()
    {
        return TransactionBeanStorage.getStorage() != null;
    }
}
