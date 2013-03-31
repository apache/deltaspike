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
import org.apache.deltaspike.core.api.jmx.MBean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

@ApplicationScoped
public class BroadcasterProducer
{
    @Inject
    private MBeanExtension extension;

    @Produces
    @Dependent
    public JmxBroadcaster jmxBroadcaster(final InjectionPoint ip)
    {
        final Class<?> declaringClass = ip.getMember().getDeclaringClass();
        final JmxBroadcaster broadcaster = extension.getBroadcasterFor(declaringClass);
        if (broadcaster == null)
        {
            //TODO discuss validation during bootstrapping
            throw new IllegalStateException("Invalid injection of " + JmxBroadcaster.class.getName() +
                    " in " + declaringClass.getName() + " detected. It is required to annotate the class with @" +
                    MBean.class.getName());
        }
        return broadcaster;
    }
}
