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
package org.apache.deltaspike.core.api.monitoring;

import javax.enterprise.inject.Typed;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class will be used as event to transport final monitor values
 *
 * @see InvocationMonitored
 */
@Typed()
public class MonitorResultEvent
{
    private Map<String, AtomicInteger> methodInvocations = new HashMap<String, AtomicInteger>();

    private Map<String, AtomicInteger> classInvocations  = new HashMap<String, AtomicInteger>();

    private Map<String, AtomicLong> methodDurations = new HashMap<String, AtomicLong>();

    public MonitorResultEvent(Map<String, AtomicInteger> methodInvocations,
                              Map<String, AtomicInteger> classInvocations,
                              Map<String, AtomicLong> methodDurations)
    {
        this.methodInvocations = methodInvocations;
        this.classInvocations = classInvocations;
        this.methodDurations = methodDurations;
    }


    /**
     * @return Map with Counters for all method invocations
     * key = fully qualified method name (includes class)
     * value = AtomicInteger with invocation count value
     */
    public Map<String, AtomicInteger> getMethodInvocations()
    {
        return methodInvocations;
    }

    /**
     * @return Map with Counter for all class invocations
     * key = fully qualified class name
     * value = AtomicInteger with invocation count value
     */
    public Map<String, AtomicInteger> getClassInvocations()
    {
        return classInvocations;
    }


    /**
     * @return Map with duration for all method invocations
     * key = fully qualified method name (includes class)
     * value = AtomicLong with duration nanos
     */
    public Map<String, AtomicLong> getMethodDurations()
    {
        return methodDurations;
    }
}
