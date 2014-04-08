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

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ProxyUtils;
import org.apache.deltaspike.jsf.api.listener.phase.AfterPhase;
import org.apache.deltaspike.jsf.api.listener.phase.BeforePhase;
import org.apache.deltaspike.jsf.api.listener.phase.JsfPhaseId;
import org.apache.deltaspike.jsf.api.listener.phase.JsfPhaseListener;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

@ApplicationScoped
public class JsfRequestLifecycleBroadcaster
{
    @Inject
    private Event<PhaseEvent> phaseEvent;

    @Inject
    @BeforePhase(JsfPhaseId.ANY_PHASE)
    private Event<PhaseEvent> beforeAnyPhaseEvent;

    @Inject
    @AfterPhase(JsfPhaseId.ANY_PHASE)
    private Event<PhaseEvent> afterAnyPhaseEvent;

    private List<PhaseListener> phaseListeners = new ArrayList<PhaseListener>();

    /**
     * Constructor used by proxy libs
     */
    protected JsfRequestLifecycleBroadcaster()
    {
    }

    @Inject
    protected JsfRequestLifecycleBroadcaster(Instance<PhaseListener> phaseListenerInstance)
    {
        Class phaseListenerClass;
        for (PhaseListener currentPhaseListener : phaseListenerInstance)
        {
            phaseListenerClass = ProxyUtils.getUnproxiedClass(currentPhaseListener.getClass());

            if (phaseListenerClass.isAnnotationPresent(JsfPhaseListener.class))
            {
                if (Deactivatable.class.isAssignableFrom(phaseListenerClass) &&
                    !ClassDeactivationUtils.isActivated(phaseListenerClass))
                {
                    continue;
                }
                this.phaseListeners.add(currentPhaseListener);
            }
        }

        //higher ordinals first
        sortDescending(this.phaseListeners);
    }

    private static void sortDescending(List<PhaseListener> phaseListeners)
    {
        Collections.sort(phaseListeners, new Comparator<PhaseListener>()
        {
            @Override
            public int compare(PhaseListener phaseListener1, PhaseListener phaseListener2)
            {
                return (phaseListener1.getClass().getAnnotation(JsfPhaseListener.class).ordinal() >
                        phaseListener2.getClass().getAnnotation(JsfPhaseListener.class).ordinal()) ? -1 : 1;
            }
        });
    }

    protected void broadcastBeforeEvent(PhaseEvent phaseEvent)
    {
        //TODO discuss exception handling

        //fire to phase-observer methods
        this.phaseEvent.select(createAnnotationLiteral(phaseEvent.getPhaseId(), true)).fire(phaseEvent);
        this.beforeAnyPhaseEvent.fire(phaseEvent);

        //fire to ds-phase-listeners
        for (PhaseListener phaseListener : this.phaseListeners)
        {
            PhaseId targetPhase = phaseListener.getPhaseId();

            if (targetPhase == PhaseId.ANY_PHASE || targetPhase == phaseEvent.getPhaseId())
            {
                phaseListener.beforePhase(phaseEvent);
            }
        }
    }

    protected void broadcastAfterEvent(PhaseEvent phaseEvent)
    {
        //TODO discuss exception handling

        //fire to phase-observer methods
        this.phaseEvent.select(createAnnotationLiteral(phaseEvent.getPhaseId(), false)).fire(phaseEvent);
        this.afterAnyPhaseEvent.fire(phaseEvent);

        //fire to ds-phase-listeners
        //call the listeners in reverse-order (like jsf)
        ListIterator<PhaseListener> phaseListenerIterator = this.phaseListeners.listIterator(phaseListeners.size());

        while (phaseListenerIterator.hasPrevious())
        {
            PhaseListener phaseListener = phaseListenerIterator.previous();
            PhaseId targetPhase = phaseListener.getPhaseId();

            if (targetPhase == PhaseId.ANY_PHASE || targetPhase == phaseEvent.getPhaseId())
            {
                phaseListener.afterPhase(phaseEvent);
            }
        }
    }

    protected Annotation createAnnotationLiteral(javax.faces.event.PhaseId phaseId, boolean isBeforeEvent)
    {
        if (isBeforeEvent)
        {
            return createBeforeLiteral(phaseId);
        }
        return createAfterLiteral(phaseId);
    }

    protected Annotation createBeforeLiteral(final javax.faces.event.PhaseId phaseId)
    {
        return new BeforePhaseBinding()
        {
            private static final long serialVersionUID = 749645435335842723L;

            @Override
            public JsfPhaseId value()
            {
                return JsfPhaseId.convertFromFacesClass(phaseId);
            }
        };
    }

    protected Annotation createAfterLiteral(final javax.faces.event.PhaseId phaseId)
    {
        return new AfterPhaseBinding()
        {
            private static final long serialVersionUID = 390037768660184656L;

            @Override
            public JsfPhaseId value()
            {
                return JsfPhaseId.convertFromFacesClass(phaseId);
            }
        };
    }
}
