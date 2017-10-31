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
package org.apache.deltaspike.test.testcontrol;

import org.apache.deltaspike.testcontrol.api.mock.DynamicMockManager;
import org.apache.deltaspike.testcontrol.impl.mock.SimpleMockManager;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Typed;
import java.lang.annotation.Annotation;

@Alternative

@RequestScoped
@Typed(DynamicMockManager.class)
public class CustomMockManager extends SimpleMockManager
{
    private static boolean isCalled;

    @Override
    public <T> T getMock(Class<T> beanClass, Annotation... qualifiers)
    {
        isCalled = true;

        return super.getMock(beanClass, qualifiers);
    }

    public static boolean isIsCalled()
    {
        return isCalled;
    }

    public static void resetInternals()
    {
        isCalled = false;
    }
}
