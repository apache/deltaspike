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
package org.apache.deltaspike.core.impl.scope;

import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;

import javax.annotation.PreDestroy;
import javax.enterprise.inject.spi.BeanManager;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractBeanHolder<K> implements Serializable
{
    private Map<K, ContextualStorage> storageMap = new ConcurrentHashMap<K, ContextualStorage>();

    private final boolean useConcurrentStorage;
    private final boolean usePassivationCapableStorage;

    protected AbstractBeanHolder()
    {
        this(true, true);
    }

    protected AbstractBeanHolder(boolean useConcurrentStorage, boolean usePassivationCapableStorage)
    {
        this.useConcurrentStorage = useConcurrentStorage;
        this.usePassivationCapableStorage = usePassivationCapableStorage;
    }

    public ContextualStorage getContextualStorage(BeanManager beanManager, K key, boolean createIfNotExist)
    {
        ContextualStorage contextualStorage = storageMap.get(key);

        if (contextualStorage == null && createIfNotExist)
        {
            contextualStorage = createContextualStorage(beanManager, key);
        }

        return contextualStorage;
    }

    protected synchronized ContextualStorage createContextualStorage(BeanManager beanManager, K key)
    {
        ContextualStorage contextualStorage = storageMap.get(key);
        if (contextualStorage == null)
        {
            contextualStorage = new ContextualStorage(beanManager, useConcurrentStorage, usePassivationCapableStorage);
            storageMap.put(key, contextualStorage);
        }
        return contextualStorage;
    }

    public Map<K, ContextualStorage> getStorageMap()
    {
        return storageMap;
    }

    public Map<K, ContextualStorage> forceNewStorage()
    {
        Map<K, ContextualStorage> oldStorageMap = storageMap;
        storageMap = new ConcurrentHashMap<K, ContextualStorage>();
        return oldStorageMap;
    }

    @PreDestroy
    public void destroyBeans()
    {
        Map<K, ContextualStorage> oldWindowContextStorages = forceNewStorage();

        for (ContextualStorage contextualStorage : oldWindowContextStorages.values())
        {
            AbstractContext.destroyAllActive(contextualStorage);
        }
    }
}
