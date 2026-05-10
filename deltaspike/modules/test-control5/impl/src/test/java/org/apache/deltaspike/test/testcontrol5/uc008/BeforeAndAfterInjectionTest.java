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
package org.apache.deltaspike.test.testcontrol5.uc008;

import org.apache.deltaspike.test.testcontrol5.shared.ApplicationScopedBean;
import org.apache.deltaspike.testcontrol5.api.junit.CdiTestExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

//Usually NOT needed! Currently only needed due to our arquillian-setup
@Tag("SeCategory")

@ExtendWith(CdiTestExtension.class)
public class BeforeAndAfterInjectionTest
{
    @Inject
    private ApplicationScopedBean applicationScopedBean;

    private Integer foundValue;

    @BeforeEach
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
        assertNotNull(this.applicationScopedBean);
        assertNotNull(this.foundValue);
    }

    @AfterEach
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
