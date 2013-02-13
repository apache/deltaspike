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
import org.apache.deltaspike.jsf.impl.security.ViewRootAccessHandler;
import org.apache.deltaspike.jsf.impl.util.SecurityUtils;
import org.apache.deltaspike.security.api.authorization.ErrorViewAwareAccessDeniedException;
import org.apache.deltaspike.security.spi.authorization.EditableAccessDecisionVoterContext;

import javax.enterprise.inject.Typed;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import java.util.logging.Logger;

@Typed() //don't use PhaseListener as type - JsfRequestLifecyclePhaseListener would fire to this listener as well
public class DeltaSpikePhaseListener implements PhaseListener, Deactivatable
{
    private static final long serialVersionUID = -4458288760053069922L;

    private final boolean activated;
    private Boolean securityModuleActivated;

    private final PhaseListener jsfRequestLifecyclePhaseListener = new JsfRequestLifecyclePhaseListener();

    public DeltaSpikePhaseListener()
    {
        this.activated = ClassDeactivationUtils.isActivated(getClass());
    }

    @Override
    public void beforePhase(PhaseEvent phaseEvent)
    {
        if (!this.activated)
        {
            return;
        }

        if (PhaseId.RENDER_RESPONSE.equals(phaseEvent.getPhaseId()))
        {
            onBeforeRenderResponse(phaseEvent.getFacesContext());
        }

        //delegate to JsfRequestLifecyclePhaseListener as a last step
        this.jsfRequestLifecyclePhaseListener.beforePhase(phaseEvent);
    }

    private void onBeforeRenderResponse(FacesContext facesContext)
    {
        checkSecuredView(facesContext);
        //TODO call pre-render-view callbacks
    }

    @Override
    public void afterPhase(PhaseEvent phaseEvent)
    {
        if (!this.activated)
        {
            return;
        }

        if (PhaseId.RESTORE_VIEW.equals(phaseEvent.getPhaseId()))
        {
            onAfterRestoreView(phaseEvent.getFacesContext());
        }

        //delegate to JsfRequestLifecyclePhaseListener as a last step
        this.jsfRequestLifecyclePhaseListener.afterPhase(phaseEvent);
    }

    private void onAfterRestoreView(FacesContext facesContext)
    {
        checkSecuredView(facesContext);
        //TODO call init-view callbacks
    }

    @Override
    public PhaseId getPhaseId()
    {
        return PhaseId.ANY_PHASE;
    }

    private void checkSecuredView(FacesContext facesContext)
    {
        if (this.securityModuleActivated == null)
        {
            lazyInit();
        }
        if (!this.securityModuleActivated)
        {
            return;
        }

        try
        {
            BeanProvider.getContextualReference(ViewRootAccessHandler.class).checkAccessTo(facesContext.getViewRoot());
        }
        catch (ErrorViewAwareAccessDeniedException accessDeniedException)
        {
            SecurityUtils.tryToHandleSecurityViolation(accessDeniedException);
            facesContext.renderResponse();
        }
    }

    private synchronized void lazyInit()
    {
        this.securityModuleActivated =
            BeanProvider.getContextualReference(EditableAccessDecisionVoterContext.class, true) != null;

        if (!this.securityModuleActivated)
        {
            Logger.getLogger(getClass().getName()) //it's the only case for which a logger is needed in this class
                    .info("security-module-impl isn't used -> " + getClass().getName() +
                            "#checkSecuredView gets deactivated");
        }
    }
}
