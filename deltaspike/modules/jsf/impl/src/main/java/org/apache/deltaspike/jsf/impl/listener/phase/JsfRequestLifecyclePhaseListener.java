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
package org.apache.deltaspike.jsf.impl.listener.phase;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

import javax.enterprise.inject.Typed;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * PhaseListener for triggering {@link JsfRequestLifecycleBroadcaster}
 */

@Typed(Deactivatable.class) //don't use PhaseListener - the broadcaster would fire to this listener as well
public class JsfRequestLifecyclePhaseListener implements PhaseListener, Deactivatable
{
    private static final long serialVersionUID = -3351903831660165998L;

    private final boolean activated;

    public JsfRequestLifecyclePhaseListener()
    {
        this.activated = ClassDeactivationUtils.isActivated(getClass());
    }

    @Override
    public void beforePhase(PhaseEvent phaseEvent)
    {
        if (this.activated)
        {
            resolveBroadcaster().broadcastBeforeEvent(phaseEvent);
        }
    }

    @Override
    public void afterPhase(PhaseEvent phaseEvent)
    {
        if (this.activated)
        {
            resolveBroadcaster().broadcastAfterEvent(phaseEvent);
        }
    }

    @Override
    public PhaseId getPhaseId()
    {
        return PhaseId.ANY_PHASE;
    }

    private JsfRequestLifecycleBroadcaster resolveBroadcaster()
    {
        //cdi has to inject the events,...
        return BeanProvider.getContextualReference(JsfRequestLifecycleBroadcaster.class);
    }
}