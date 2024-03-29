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
package org.apache.deltaspike.test.core.impl.custom.spi;

import org.apache.deltaspike.core.util.ServiceUtils;
import org.junit.Assert;
import org.junit.Test;

public abstract class ServiceUtilsTest
{
    @Test
    public void lookupOfSpiImplementations()
    {
        Assert.assertTrue(ServiceUtils.loadServiceImplementations(MyInterface.class).iterator().hasNext());

        Assert.assertNotNull(ServiceUtils.loadServiceImplementations(MyInterface.class));
        Assert.assertFalse(ServiceUtils.loadServiceImplementations(MyInterface.class).isEmpty());
        Assert.assertEquals(1, ServiceUtils.loadServiceImplementations(MyInterface.class).size());

        Assert.assertEquals(
            "test", ServiceUtils.loadServiceImplementations(MyInterface.class).iterator().next().getValue());
    }
}
