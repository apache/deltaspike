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
package org.apache.deltaspike.core.impl.util;

import org.apache.deltaspike.core.api.activation.ClassDeactivator;

import java.util.Set;

/**
 * Helper implementation which gets initialized with all configured classes which have to be deactivated
 */
class DefaultClassDeactivator implements ClassDeactivator
{
    private static final long serialVersionUID = -1653478265237950470L;

    private Set<Class> deactivatedClasses;

    /**
     * Required constructor which receives all configured classes which will be returned by #getDeactivatedClasses
     * @param deactivatedClasses classes which get returned by #getDeactivatedClasses
     */
    public DefaultClassDeactivator(Set<Class> deactivatedClasses)
    {
        this.deactivatedClasses = deactivatedClasses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class> getDeactivatedClasses()
    {
        return this.deactivatedClasses;
    }
}
