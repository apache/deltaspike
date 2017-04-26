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

import org.apache.deltaspike.core.api.jmx.JmxBroadcaster;
import org.apache.deltaspike.core.api.jmx.JmxManaged;
import org.apache.deltaspike.core.api.jmx.JmxParameter;
import org.apache.deltaspike.core.api.jmx.MBean;
import org.apache.deltaspike.core.api.jmx.Table;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.management.Notification;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
@MBean(description = "my mbean")
public class MyMBean
{
    @JmxManaged(description = "get counter")
    private int counter = 0;

    @Inject
    private JmxBroadcaster broadcaster;

    @JmxManaged
    private Map<String, String> table;

    @JmxManaged // just a marker to expose it as an attribute, will call the getter
    private Table table2;

    public Map<String, String> getTable() {
        return table != null ? table : (table = new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
        }});
    }

    @JmxManaged
    public Table getTable2() {
        return new Table().withColumns("a", "b", "c").withLine("1", "2", "3").withLine("alpha", "beta", "gamma");
    }

    public int getCounter()
    {
        return counter;
    }

    public void setCounter(final int v)
    {
        counter = v;
    }

    public void resetTo(final int value)
    {
        counter = value;
    }

    @JmxManaged(description = "multiply counter")
    public int multiply(@JmxParameter(name = "multiplier", description = "the multiplier") final int n)
    {
        return counter * n;
    }

    public void broadcast()
    {
        broadcaster.send(new Notification(String.class.getName(), this, 10L));
    }
}
