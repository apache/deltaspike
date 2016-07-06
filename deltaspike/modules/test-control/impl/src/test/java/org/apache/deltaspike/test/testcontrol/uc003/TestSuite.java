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
package org.apache.deltaspike.test.testcontrol.uc003;

import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestSuiteRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.apache.deltaspike.test.testcontrol.shared.ApplicationScopedBean;

//Usually NOT needed! Currently only needed due to our arquillian-setup
@Category(SeCategory.class)
@RunWith(CdiTestSuiteRunner.class) //starts container once (for the whole suite)
@Suite.SuiteClasses({
        RequestAndSessionScopePerTestMethodTest.class,
        SessionScopePerTestClassTest.class
})
public class TestSuite
{
    @BeforeClass
    public static void resetSharedState()
    {
        ApplicationScopedBean.resetInstanceCount();
    }

    @AfterClass
    public static void finalCheckAndCleanup()
    {
        if (ApplicationScopedBean.getInstanceCount() != 1)
        {
            throw new IllegalStateException("unexpected count");
        }
        ApplicationScopedBean.resetInstanceCount();
    }
}
