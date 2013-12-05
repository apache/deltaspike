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
package org.apache.deltaspike.test.testcontrol.shared;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApplicationScopedBean
{
    private int count = 0;
    private static int instanceCount = 0;

    @PostConstruct
    protected void init()
    {
        instanceCount++;
    }

    public int getCount()
    {
        return count;
    }

    public void increaseCount()
    {
        this.count++;
    }

    public void resetCount()
    {
        this.count = 0;
    }

    public static int getInstanceCount()
    {
        return instanceCount;
    }

    public static void resetInstanceCount()
    {
        instanceCount = 0;
    }
}
