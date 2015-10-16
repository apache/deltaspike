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
package org.apache.deltaspike.test.core.api.partialbean.uc009;

import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

public abstract class PartialBeanWithProducerTest
{
    @Inject
    private TestConfig testConfig;

    @Inject
    @TestValue
    private String value2;

    @Inject
    private TestCustomType value4;

    @Test
    public void testPartialBeanWithProducerManualAccess() throws Exception
    {
        Assert.assertEquals(new Integer(1), testConfig.value1());
        Assert.assertEquals("2", testConfig.value2());
        Assert.assertEquals(new Integer(3), testConfig.value3());
        Assert.assertEquals(new Integer(4), testConfig.value4().getTestValue());
    }

    @Test
    public void testProducedResultOfPartialBeanWithProducer() throws Exception
    {
        Assert.assertEquals("2", value2);
        Assert.assertEquals(new Integer(4), value4.getTestValue());
    }
}
