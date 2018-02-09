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
package org.apache.deltaspike.test.core.impl.interdyn;

import org.apache.deltaspike.core.api.monitoring.MonitorResultEvent;
import org.junit.Assert;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class SomeTestService
{

    private boolean check = false;

    public String pingA()
    {
        return "a";
    }

    public String pingB()
    {
        try
        {
            Thread.sleep(30L);
        }
        catch (InterruptedException e) {
            // all fine
        }
        return "b";
    }

    public void enableChecking()
    {
        this.check = true;
    }

    public void observer(@Observes MonitorResultEvent mre)
    {
        if (check)
        {
            Assert.assertTrue(mre.getClassInvocations().keySet().contains(SomeTestService.class.getName()));
            check = false;
        }
    }
}
