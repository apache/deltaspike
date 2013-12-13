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
package org.apache.deltaspike.servlet.impl.event;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

/**
 * Base class for classes which send servlet events to the CDI event bus. This class uses {@link BeanManagerProvider} to
 * obtain the BeanManager.
 */
abstract class EventBroadcaster implements Deactivatable
{

    private volatile BeanManager beanManager;

    private final boolean activated;

    public EventBroadcaster()
    {
        this.activated = ClassDeactivationUtils.isActivated(getClass());
    }

    protected void fireEvent(Object event, Annotation... qualifier)
    {
        getBeanManager().fireEvent(event, qualifier);
    }

    protected BeanManager getBeanManager()
    {
        if (beanManager == null)
        {
            synchronized (this)
            {
                if (beanManager == null)
                {
                    beanManager = BeanManagerProvider.getInstance().getBeanManager();
                }
            }
        }

        return beanManager;
    }

    protected boolean isActivated()
    {
        return activated;
    }

}
