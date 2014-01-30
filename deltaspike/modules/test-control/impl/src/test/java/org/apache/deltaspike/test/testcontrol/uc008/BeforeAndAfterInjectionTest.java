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
package org.apache.deltaspike.test.testcontrol.uc008;

import junit.framework.Assert;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.testcontrol.shared.ApplicationScopedBean;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;

//Usually NOT needed! Currently only needed due to our arquillian-setup
@Category(SeCategory.class)



@RunWith(CdiTestRunner.class)
public class BeforeAndAfterInjectionTest
{
    @Inject
    private ApplicationScopedBean applicationScopedBean;

    private Integer foundValue;

    @Before
    public void before()
    {
        if (this.applicationScopedBean == null)
        {
            throw new IllegalStateException("injection failed");
        }

        this.foundValue = this.applicationScopedBean.getCount();
    }

    @Test
    public void injectionTest()
    {
        Assert.assertNotNull(this.applicationScopedBean);
        Assert.assertNotNull(this.foundValue);
    }

    @After
    public void after()
    {
        if (this.applicationScopedBean == null)
        {
            throw new IllegalStateException("injection failed");
        }
        if (this.foundValue == null)
        {
            throw new IllegalStateException("different instance without initialized value found");
        }
    }
}
