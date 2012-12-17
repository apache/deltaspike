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
package org.apache.deltaspike.core.impl.jmx;

import org.apache.deltaspike.core.api.jmx.JmxBroadcaster;
import org.apache.deltaspike.core.api.jmx.annotation.Jmx;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

public class BroadcasterProducer
{
    @Inject
    private MBeanExtension extension;

    @Jmx
    @Produces
    public JmxBroadcaster produces(final InjectionPoint ip)
    {
        final Class<?> declaringClass = ip.getMember().getDeclaringClass();
        final DynamicMBeanWrapper wrapperFor = extension.getWrapperFor(declaringClass);
        if (wrapperFor == null)
        {
            throw new IllegalArgumentException("Can't inject a JmxBroadcaster in " + declaringClass.getName());
        }
        return wrapperFor;
    }
}
