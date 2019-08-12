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

import org.apache.deltaspike.core.api.config.view.controller.InitView;
import org.apache.deltaspike.core.api.config.view.controller.PostRenderView;
import org.apache.deltaspike.core.api.config.view.controller.PreRenderView;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.jsf.impl.security.ViewRootAccessHandler;
import org.apache.deltaspike.jsf.impl.util.JsfUtils;
import org.apache.deltaspike.jsf.impl.util.SecurityUtils;
import org.apache.deltaspike.jsf.impl.util.ViewControllerUtils;
import org.apache.deltaspike.security.api.authorization.ErrorViewAwareAccessDeniedException;
import org.apache.deltaspike.security.spi.authorization.EditableAccessDecisionVoterContext;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Typed;
import javax.faces.component.UIViewRoot;
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

    private volatile ViewConfigResolver viewConfigResolver;

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

        if (this.viewConfigResolver == null)
        {
            lazyInit();
        }

        processInitView(phaseEvent);

        //delegate to JsfRequestLifecyclePhaseListener as a last step
        this.jsfRequestLifecyclePhaseListener.beforePhase(phaseEvent);

        if (PhaseId.RENDER_RESPONSE.equals(phaseEvent.getPhaseId()))
        {
            onBeforeRenderResponse(phaseEvent.getFacesContext());
        }
    }

    private void onBeforeRenderResponse(FacesContext facesContext)
    {
        checkSecuredView(facesContext);
        processPreRenderView(facesContext);
    }

    @Override
    public void afterPhase(PhaseEvent phaseEvent)
    {
        if (!this.activated)
        {
            return;
        }

        if (this.viewConfigResolver == null)
        {
            lazyInit();
        }

        processInitView(phaseEvent);

        if (PhaseId.RESTORE_VIEW.equals(phaseEvent.getPhaseId()))
        {
            onAfterRestoreView(phaseEvent.getFacesContext());

        }
        else if (PhaseId.RENDER_RESPONSE.equals(phaseEvent.getPhaseId()))
        {
            onAfterRenderResponse(phaseEvent.getFacesContext());
        }

        //delegate to JsfRequestLifecyclePhaseListener as a last step
        this.jsfRequestLifecyclePhaseListener.afterPhase(phaseEvent);
    }

    private void onAfterRestoreView(FacesContext facesContext)
    {
        JsfUtils.tryToRestoreMessages(facesContext);
    }

    private void onAfterRenderResponse(FacesContext facesContext)
    {
        processPostRenderView(facesContext);
    }

    @Override
    public PhaseId getPhaseId()
    {
        return PhaseId.ANY_PHASE;
    }

    private void checkSecuredView(FacesContext facesContext)
    {
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
        if (this.viewConfigResolver != null)
        {
            return;
        }

        this.securityModuleActivated =
            BeanProvider.getContextualReference(EditableAccessDecisionVoterContext.class, true) != null;

        this.viewConfigResolver = BeanProvider.getContextualReference(ViewConfigResolver.class);

        if (!this.securityModuleActivated)
        {
            Logger.getLogger(getClass().getName()) //it's the only case for which a logger is needed in this class
                    .info("security-module-impl isn't used -> " + getClass().getName() +
                            "#checkSecuredView gets deactivated");
        }
    }

    private void processInitView(PhaseEvent event)
    {
        if (event.getPhaseId().equals(PhaseId.RESTORE_VIEW) && !isRedirectRequest(event.getFacesContext()))
        {
            return;
        }

        //TODO check if we have to restrict the other callbacks as well
        //leads to a call of @BeforePhase but not the corresponding @AfterPhase call of the corresponding callbacks

        //TODO don't call the callbacks in case of an initial redirct
        //was:
        /*
        if(Boolean.TRUE.equals(event.getFacesContext().getExternalContext().getRequestMap()
                .get(WindowContextManagerObserver.INITIAL_REDIRECT_PERFORMED_KEY)))
        {
            return;
        }
        */

        FacesContext facesContext = event.getFacesContext();
        if (facesContext.getViewRoot() != null && facesContext.getViewRoot().getViewId() != null)
        {
            processInitView(event.getFacesContext().getViewRoot().getViewId());
        }
    }

    private void processInitView(String viewId)
    {
        try
        {
            WindowMetaData windowMetaData = BeanProvider.getContextualReference(WindowMetaData.class);

            //view already initialized in this or any prev. request
            if (viewId.equals(windowMetaData.getInitializedViewId()))
            {
                return;
            }

            //override the view-id if we have a new view
            windowMetaData.setInitializedViewId(viewId);

            ViewConfigDescriptor viewDefinitionEntry = this.viewConfigResolver.getViewConfigDescriptor(viewId);

            if (viewDefinitionEntry == null)
            {
                return;
            }

            ViewControllerUtils.executeViewControllerCallback(viewDefinitionEntry, InitView.class);
        }
        catch (ContextNotActiveException e)
        {
            //TODO discuss how we handle it
        }
    }

    private void processPreRenderView(FacesContext facesContext)
    {
        UIViewRoot uiViewRoot = facesContext.getViewRoot();

        if (uiViewRoot != null)
        {
            ViewConfigDescriptor viewDefinitionEntry =
                    this.viewConfigResolver.getViewConfigDescriptor(uiViewRoot.getViewId());

            ViewControllerUtils.executeViewControllerCallback(viewDefinitionEntry, PreRenderView.class);
        }
    }

    private void processPostRenderView(FacesContext facesContext)
    {
        UIViewRoot uiViewRoot = facesContext.getViewRoot();

        if (uiViewRoot != null)
        {
            ViewConfigDescriptor viewDefinitionEntry =
                    this.viewConfigResolver.getViewConfigDescriptor(uiViewRoot.getViewId());

            ViewControllerUtils.executeViewControllerCallback(viewDefinitionEntry, PostRenderView.class);
        }
    }

    private boolean isRedirectRequest(FacesContext facesContext)
    {
        return facesContext.getResponseComplete();
    }
}
