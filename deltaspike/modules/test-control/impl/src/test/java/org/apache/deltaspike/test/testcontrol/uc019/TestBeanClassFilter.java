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

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.spi.filter.ClassFilter;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.testcontrol.api.TestControl;

//!!!not!!! needed with cdi 1.1+ and @Priority (which is the target of this use-case)
//only needed because our test-suite is based on cdi v1.0

//also useful to test DELTASPIKE-1337
public class TestBeanClassFilter implements ClassFilter
{
    @Override
    public boolean isFiltered(Class<?> targetClass)
    {
        if (!targetClass.getName().startsWith("org.apache.deltaspike.test."))
        {
            return false;
        }

        String currentTestOrigin = ConfigResolver.getPropertyValue(TestControl.class.getName());

        if (currentTestOrigin == null) //no known origin (no @TestControl is used)
        {
            //filter all classes which are located in packages using tests with class-filters
            //(since we test the feature with ambiguous beans which isn't valid without filtering)
            return getClass().getPackage().getName().equals(targetClass.getPackage().getName());
        }
        else
        {
            Class<?> currentOrigin = ClassUtils.tryToLoadClassForName(currentTestOrigin);
            //origin is in one of the packages for class-filtering tests
            if (getClass().getPackage().getName().equals(currentOrigin.getPackage().getName()))
            {
                TestControl testControl = currentOrigin.getAnnotation(TestControl.class);
                return ClassUtils.tryToInstantiateClass(testControl.classFilter()).isFiltered(targetClass);
            }
            return isInSamePackage(targetClass);
        }
    }

    private boolean isInSamePackage(Class<?> targetClass)
    {
        return targetClass.getPackage().getName().equals(getClass().getPackage().getName());
    }
}
