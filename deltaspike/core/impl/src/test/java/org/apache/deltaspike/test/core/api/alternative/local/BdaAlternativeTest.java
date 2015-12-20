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
package org.apache.deltaspike.test.core.api.alternative.local;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

/**
 * Keep in sync with {@link org.apache.deltaspike.test.core.api.alternative.global.GlobalAlternativeTest} -
 * but without configuring the alternatives and check for the default implementations.
 *
 * Tests which checks the behaviour with deactivated global alternates.
 */
public abstract class BdaAlternativeTest
{
    @Inject
    private BaseInterface2 bean;

    /*
     * The default implementation should be found
     */
    @Test
    public void alternativeImplementationWithClassAsBaseType()
    {
        List<BaseBean2> testBeans = BeanProvider.getContextualReferences(BaseBean2.class, true);

        Assert.assertEquals(1, testBeans.size());
        Assert.assertEquals(BaseBean2.class.getName(), testBeans.get(0).getClass().getName());
    }

    /*
     * The default implementation should be found
     */
    @Test
    public void alternativeImplementationWithInterfaceAsBaseType()
    {
        Assert.assertEquals(BaseInterface2DefaultImplementation.class.getName(), bean.getClass().getName());
    }
}
