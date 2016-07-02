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
package org.apache.deltaspike.test.testcontrol.mock.uc013;

import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.apache.deltaspike.testcontrol.api.mock.DynamicMockManager;
import org.apache.deltaspike.testcontrol.api.mock.TypedMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;

//Usually NOT needed! Currently only needed due to our arquillian-setup
@Category(SeCategory.class)

@RunWith(CdiTestRunner.class)
public class MockedTypedProducedBeanTest
{
    @Inject
    private T1 t1;

    @Inject
    private T2 t2;

    @Inject
    private T3 t3;

    @Inject
    private DynamicMockManager mockManager;

    @Test
    public void manualMockT1()
    {
        mockManager.addMock(new MockedTypedBean1and2(7));
        mockManager.addMock(new MockedTypedBean3(14));
        Assert.assertEquals(7, t1.getCount());
        Assert.assertEquals(7, t2.getCount());
        Assert.assertEquals(14, t3.getCount());
    }

    @TypedMock({T1.class, T2.class}) //specify the types for mocking (to replace the producer)
    private static class MockedTypedBean1and2 extends TypedBean1and2
    {
        private final int mockedCount;

        private MockedTypedBean1and2(int mockedCount) //-> no @Typed() needed
        {
            this.mockedCount = mockedCount;
        }

        @Override
        public int getCount()
        {
            return mockedCount;
        }
    }

     //exclude it for the cdi type-check
    @TypedMock(T3.class)
    private static class MockedTypedBean3 extends TypedBean3
    {
        private final int mockedCount;

        private MockedTypedBean3(int mockedCount) //-> no @Typed() needed
        {
            this.mockedCount = mockedCount;
        }

        @Override
        public int getCount()
        {
            return mockedCount;
        }
    }
}
