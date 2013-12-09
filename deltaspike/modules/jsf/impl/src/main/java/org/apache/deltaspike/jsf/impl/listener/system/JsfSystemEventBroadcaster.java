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
package org.apache.deltaspike.jsf.impl.listener.system;

import javax.enterprise.inject.spi.BeanManager;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

/**
 * Broadcasts JSF events to CDI observers.
 */
public class JsfSystemEventBroadcaster implements SystemEventListener, Deactivatable
{
    private boolean isActivated = true;

    public JsfSystemEventBroadcaster()
    {
        this.isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    @Override
    public boolean isListenerForSource(Object source)
    {
        return true;
    }

    @Override
    public void processEvent(SystemEvent e) throws AbortProcessingException
    {
        if (!this.isActivated)
        {
            return;
        }

        BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();
        beanManager.fireEvent(e);
    }
}
