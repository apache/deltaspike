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
package org.apache.deltaspike.test.core.impl.jmx;

import org.apache.deltaspike.core.api.jmx.JmxManaged;
import org.apache.deltaspike.core.api.jmx.MBean;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@MBean(category = "cat", type = "and", name = "", properties = "foo=bar,dummy=empty")
public class CustomProperties
{
    @JmxManaged
    private int counter;

    public int getCounter()
    {
        return counter;
    }

    public void setCounter(final int counter)
    {
        this.counter = counter;
    }
}
