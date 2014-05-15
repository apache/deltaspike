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
package org.apache.deltaspike.test.testcontrol.mock.uc003;

import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.testcontrol.mock.shared.SessionScopedBean;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.apache.deltaspike.testcontrol.api.mock.DynamicMockManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

//Usually NOT needed! Currently only needed due to our arquillian-setup
@Category(SeCategory.class)

@RunWith(CdiTestRunner.class)
@TestControl(startScopes = {ApplicationScoped.class, SessionScoped.class})
public class MockedSessionScopedBeanAcrossMethodsTest
{
    @Inject
    private SessionScopedBean sessionScopedBean;

    @Inject
    private DynamicMockManager mockManager;

    //static is needed here, because the session spans across all test-methods
    private static MockedSessionScopedBean mockedSessionScopedBean = new MockedSessionScopedBean();

    @Before
    public void init()
    {
        mockManager.addMock(mockedSessionScopedBean);
    }

    @Test
    public void manualMock1()
    {
        mockedSessionScopedBean.setCount(7);

        Assert.assertEquals(7, sessionScopedBean.getCount());
        sessionScopedBean.increaseCount();
        Assert.assertEquals(8, sessionScopedBean.getCount());
    }

    @Test
    public void manualMock2()
    {
        mockedSessionScopedBean.setCount(14);

        Assert.assertEquals(14, sessionScopedBean.getCount());
        sessionScopedBean.increaseCount();
        Assert.assertEquals(15, sessionScopedBean.getCount());
    }
}
