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
package org.apache.deltaspike.test.testcontrol.uc019;

import org.apache.deltaspike.core.spi.filter.ClassFilter;
import org.apache.deltaspike.testcontrol.api.TestControl;

import javax.enterprise.inject.Alternative;

public abstract class TestLabeledAlternativeFilter implements ClassFilter
{
    private final Class<? extends TestControl.Label> activeLabel;

    protected TestLabeledAlternativeFilter(Class<? extends TestControl.Label> activeLabel)
    {
        this.activeLabel = activeLabel;
    }

    @Override
    public boolean isFiltered(Class<?> targetClass)
    {
        if (!targetClass.isAnnotationPresent(Alternative.class))
        {
            return false;
        }

        TestLabeled testLabeled = targetClass.getAnnotation(TestLabeled.class);

        if (testLabeled == null)
        {
            return false;
        }

        if (testLabeled.value().equals(activeLabel))
        {
            return false;
        }
        return true;
    }
}
