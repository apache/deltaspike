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
package org.apache.deltaspike.test.testcontrol.mock.uc012;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.testcontrol.mock.shared.RequestScopedBean;
import org.apache.deltaspike.test.testcontrol.mock.shared.SessionScopedBean;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.apache.deltaspike.testcontrol.api.mock.DynamicMockManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.mockito.Mockito.*;

//Usually NOT needed! Currently only needed due to our arquillian-setup
@Category(SeCategory.class)

@RunWith(CdiTestRunner.class)
public class MockedRequestScopedBeanWithInjectionTest
{
    @Inject
    private RequestScopedBean requestScopedBean;

    @Inject
    private SessionScopedBean sessionScopedBean;

    @Inject
    private DynamicMockManager mockManager;

    @Test
    public void mixedMocksWithInjection1()
    {
        SessionScopedBean mockedSessionScopedBean = mock(SessionScopedBean.class);
        when(mockedSessionScopedBean.getCount()).thenReturn(7);
        mockManager.addMock(mockedSessionScopedBean);

        RequestScopedBean mockedRequestScopedBean = new MockedRequestScopedBeanWithInjection();
        BeanProvider.injectFields(mockedRequestScopedBean);
        mockManager.addMock(mockedRequestScopedBean);

        Assert.assertEquals(7, requestScopedBean.getCount());
        requestScopedBean.increaseCount(); //not delegated
        Assert.assertEquals(7, requestScopedBean.getCount());
        sessionScopedBean.increaseCount();
        Assert.assertEquals(7, sessionScopedBean.getCount()); //still mocked
        Assert.assertEquals(7, requestScopedBean.getCount()); //still mocked
    }

    @Test
    public void mixedMocksWithInjection2()
    {
        SessionScopedBean mockedSessionScopedBean = mock(SessionScopedBean.class);
        when(mockedSessionScopedBean.getCount()).thenReturn(14);
        mockManager.addMock(mockedSessionScopedBean);

        RequestScopedBean mockedRequestScopedBean = new MockedRequestScopedBeanWithInjection();
        BeanProvider.injectFields(mockedRequestScopedBean);
        mockManager.addMock(mockedRequestScopedBean);

        Assert.assertEquals(14, requestScopedBean.getCount());
        requestScopedBean.increaseCount(); //not delegated
        Assert.assertEquals(14, requestScopedBean.getCount());
        sessionScopedBean.increaseCount();
        Assert.assertEquals(14, sessionScopedBean.getCount()); //still mocked
        Assert.assertEquals(14, requestScopedBean.getCount()); //still mocked
    }
}
