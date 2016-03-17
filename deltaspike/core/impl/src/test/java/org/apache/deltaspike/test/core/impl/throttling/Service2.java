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
package org.apache.deltaspike.test.core.impl.throttling;

import org.apache.deltaspike.core.api.throttling.Throttled;
import org.apache.deltaspike.core.api.throttling.Throttling;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collection;

@Throttling(permits = 2)
@ApplicationScoped
public class Service2
{
    private final Collection<String> called = new ArrayList<String>();

    @Throttled(timeout = 750)
    public void call(final String k)
    {
        synchronized (called)
        {
            called.add(k);
        }
        try
        {
            Thread.sleep(1000);
        }
        catch (final InterruptedException e)
        {
            Thread.interrupted();
        }
    }

    @Throttled(weight = 2)
    public void heavy(final Runnable inTask)
    {
        inTask.run();
        try
        {
            Thread.sleep(5000);
        }
        catch (final InterruptedException e)
        {
            Thread.interrupted();
        }
    }

    public Collection<String> getCalled()
    {
        return called;
    }
}
