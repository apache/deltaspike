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
package org.apache.deltaspike.test.testcontrol5.uc001;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.test.testcontrol5.shared.ApplicationScopedBean;
import org.apache.deltaspike.test.testcontrol5.shared.RequestScopedBean;
import org.apache.deltaspike.test.testcontrol5.shared.SessionScopedBean;
import org.apache.deltaspike.test.testcontrol5.shared.TestUtils;
import org.apache.deltaspike.testcontrol5.api.junit.CdiTestExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

//Usually NOT needed! Currently only needed due to our arquillian-setup

@ExtendWith(CdiTestExtension.class) //starts container once and one session + request per test-method
//implicitly annotated with @TestControl without the default-scope settings
public class RequestAndSessionScopePerTestMethodTest
{
    @Inject
    private ApplicationScopedBean applicationScopedBean;

    @Inject
    private SessionScopedBean sessionScopedBean;

    @Inject
    private RequestScopedBean requestScopedBean;

    @Test
    //implicitly annotated with @TestControl and its default-values
    public void firstTest()
    {
        applicationScopedBean.increaseCount();

        assertEquals(0, requestScopedBean.getCount());
        requestScopedBean.increaseCount();
        assertEquals(1, requestScopedBean.getCount());

        assertEquals(0, sessionScopedBean.getCount());
        sessionScopedBean.increaseCount();
        assertEquals(1, sessionScopedBean.getCount());
    }

    @Test
    //implicitly annotated with @TestControl and its default-values
    public void secondTest()
    {
        applicationScopedBean.increaseCount();

        assertEquals(0, requestScopedBean.getCount());
        requestScopedBean.increaseCount();
        assertEquals(1, requestScopedBean.getCount());

        assertEquals(0, sessionScopedBean.getCount());
        sessionScopedBean.increaseCount();
        assertEquals(1, sessionScopedBean.getCount());
    }

    @BeforeAll
    public static void resetSharedState()
    {
        BeanProvider.getContextualReference(ApplicationScopedBean.class).resetCount();
        RequestScopedBean.resetInstanceCount();
    }

    @AfterAll
    public static void finalCheckAndCleanup()
    {
        int testCount = TestUtils.getTestMethodCount(RequestAndSessionScopePerTestMethodTest.class);

        if (RequestScopedBean.getInstanceCount() != testCount)
        {
            throw new IllegalStateException("unexpected instance count");
        }
        RequestScopedBean.resetInstanceCount();

        if (BeanProvider.getContextualReference(ApplicationScopedBean.class).getCount() != testCount)
        {
            throw new IllegalStateException("unexpected count");
        }
        BeanProvider.getContextualReference(ApplicationScopedBean.class).resetCount();
    }
}
