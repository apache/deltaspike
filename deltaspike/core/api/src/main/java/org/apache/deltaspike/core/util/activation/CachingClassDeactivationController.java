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

package org.apache.deltaspike.core.util.activation;

import org.apache.deltaspike.core.spi.activation.Deactivatable;

import javax.enterprise.inject.Typed;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Typed()
public class CachingClassDeactivationController extends BaseClassDeactivationController
{
    /**
     * Cache for the result. It won't contain many classes but it might be accessed frequently.
     * Valid entries are only true or false. If an entry isn't available or null, it gets calculated.
     */
    private Map<Class<? extends Deactivatable>, Boolean> activationStatusCache
        = new ConcurrentHashMap<Class<? extends Deactivatable>, Boolean>();

    /**
     * Evaluates if the given {@link Deactivatable} is active.
     *
     * @param targetClass {@link Deactivatable} under test.
     * @return <code>true</code> if it is active, <code>false</code> otherwise
     */
    public boolean isActivated(Class<? extends Deactivatable> targetClass)
    {
        Boolean activatedClassCacheEntry = activationStatusCache.get(targetClass);

        if (activatedClassCacheEntry == null)
        {
            activatedClassCacheEntry = loadAndCacheResult(targetClass);
        }
        return activatedClassCacheEntry;
    }

    private synchronized boolean loadAndCacheResult(Class<? extends Deactivatable> targetClass)
    {
        boolean activatedClassCacheEntry = BaseClassDeactivationController.calculateDeactivationStatusFor(targetClass);
        activationStatusCache.put(targetClass, activatedClassCacheEntry);
        return activatedClassCacheEntry;
    }
}
