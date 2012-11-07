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
package org.apache.deltaspike.core.impl.scope.window;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.BeanManager;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.deltaspike.core.util.context.ContextualStorage;

/**
 * This holder will store the window Ids for the current
 * Session. We use standard SessionScoped bean to not need
 * to treat async-supported and similar headache.
 */
@SessionScoped
public class WindowBeanHolder implements Serializable
{
    private volatile Map<String, ContextualStorage> storageMap = new ConcurrentHashMap<String, ContextualStorage>();

    public Map<String, ContextualStorage> getStorageMap()
    {
        return storageMap;
    }

    /**
     * This method will return the ContextualStorage or create a new one
     * if no one is yet assigned to the current windowId.
     */
    public ContextualStorage getContextualStorage(BeanManager beanManager, String windowId)
    {
        ContextualStorage contextualStorage = storageMap.get(windowId);
        if (contextualStorage == null)
        {
            synchronized (this)
            {
                contextualStorage = storageMap.get(windowId);
                if (contextualStorage == null)
                {
                    storageMap.put(windowId, new ContextualStorage(beanManager, true, true));
                }
            }
        }

        return contextualStorage;
    }

    public Map<String, ContextualStorage> newStorageMap()
    {
        Map<String, ContextualStorage> oldStorageMap = storageMap;
        storageMap = new ConcurrentHashMap<String, ContextualStorage>();
        return oldStorageMap;
    }
}
