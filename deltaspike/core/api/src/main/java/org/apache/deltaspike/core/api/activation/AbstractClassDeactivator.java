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
package org.apache.deltaspike.core.api.activation;

import java.util.HashSet;
import java.util.Set;

/**
 * Base implementation which allows an easier class-deactivator implementation
 */
public abstract class AbstractClassDeactivator implements ClassDeactivator
{
    //HashSet due to Serializable warning in checkstyle rules
    private HashSet<Class> deactivatedClasses = null;

    /**
     * {@inheritDoc}
     */
    public final Set<Class> getDeactivatedClasses()
    {
        if (this.deactivatedClasses == null)
        {
            this.deactivatedClasses = new HashSet<Class>();
            deactivateClasses();
        }
        return this.deactivatedClasses;
    }

    /**
     * Can be used by sub-classes to add deactivated classes easily.
     *
     * @param deactivatedClass class to deactivate
     */
    protected final void addDeactivatedClass(Class deactivatedClass)
    {
        this.deactivatedClasses.add(deactivatedClass);
    }

    /**
     * An implementation has to add classes which shouldn't be used by DeltaSpike.
     * (use {@link #addDeactivatedClass(Class)} for adding classes)
     */
    protected abstract void deactivateClasses();
}
