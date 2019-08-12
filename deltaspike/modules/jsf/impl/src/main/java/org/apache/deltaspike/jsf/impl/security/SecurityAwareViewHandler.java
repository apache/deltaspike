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
package org.apache.deltaspike.jsf.impl.security;

import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.jsf.impl.util.SecurityUtils;
import org.apache.deltaspike.security.api.authorization.ErrorViewAwareAccessDeniedException;
import org.apache.deltaspike.security.spi.authorization.EditableAccessDecisionVoterContext;

import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SecurityAwareViewHandler extends ViewHandlerWrapper implements Deactivatable
{
    public  static final String PRE_DESTROY_VIEW_MAP_EVENT_FILTER_ENABLED =
            SecurityAwareViewHandler.class.getName() + "#PRE_DESTROY_VIEW_MAP_EVENT_FILTER_ENABLED";

    protected final ViewHandler wrapped;

    private final boolean activated;
    private volatile Boolean securityModuleActivated;

    /**
     * Constructor for wrapping the given {@link ViewHandler}
     *
     * @param wrapped view-handler which should be wrapped
     */
    public SecurityAwareViewHandler(ViewHandler wrapped)
    {
        this.wrapped = wrapped;

        this.activated = ClassDeactivationUtils.isActivated(getClass());
    }

    @Override
    public ViewHandler getWrapped()
    {
        return this.wrapped;
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId)
    {
        UIViewRoot result = this.wrapped.createView(context, viewId);

        if (!this.activated)
        {
            return result;
        }

        if (this.securityModuleActivated == null)
        {
            lazyInit();
        }
        if (!this.securityModuleActivated)
        {
            return result;
        }

        UIViewRoot originalViewRoot = context.getViewRoot();

        Map<String, Object> viewMap = null;
        if (originalViewRoot != null)
        {
            Map<String, Object> originalViewMap = originalViewRoot.getViewMap(false);

            if (originalViewMap != null && !originalViewMap.isEmpty())
            {
                viewMap = new HashMap<String, Object>();
                viewMap.putAll(originalViewMap);
            }
        }

        //workaround for PreDestroyViewMapEvent which would be caused by the security check
        deactivatePreDestroyViewMapEvent(context);

        //we have to use it as current view if an AccessDecisionVoter uses the JSF API to check access to the view-id
        context.setViewRoot(result);

        try
        {
            ViewRootAccessHandler viewRootAccessHandler =
                    BeanProvider.getContextualReference(ViewRootAccessHandler.class);

            viewRootAccessHandler.checkAccessTo(result);
        }
        catch (ErrorViewAwareAccessDeniedException accessDeniedException)
        {
            ViewConfigResolver viewConfigResolver = BeanProvider.getContextualReference(ViewConfigResolver.class);

            ViewConfigDescriptor errorViewDescriptor = viewConfigResolver
                    .getViewConfigDescriptor(accessDeniedException.getErrorView());

            try
            {
                if (errorViewDescriptor != null && View.NavigationMode.REDIRECT ==
                    errorViewDescriptor.getMetaData(View.class).iterator().next().navigation() /*always available*/ &&
                    BeanProvider.getContextualReference(JsfModuleConfig.class)
                        .isAlwaysUseNavigationHandlerOnSecurityViolation())
                {
                    SecurityUtils.tryToHandleSecurityViolation(accessDeniedException);
                }
                else
                {
                    SecurityUtils.handleSecurityViolationWithoutNavigation(accessDeniedException);
                }
            }
            finally
            {
                broadcastAccessDeniedException(accessDeniedException);
            }

            if (errorViewDescriptor != null)
            {
                return this.wrapped.createView(context, errorViewDescriptor.getViewId());
            }
            else
            {
                //only in case of GET requests, because an exception during POST requests leads to re-rendering
                //the previous page (including the error message)
                if (!context.isPostback() && context.getViewRoot() != null)
                {
                    context.getViewRoot().setViewId(null);
                }
            }
            throw accessDeniedException; //security exception without error-view
        }
        finally
        {
            activatePreDestroyViewMapEvent(context);
            if (originalViewRoot != null)
            {
                context.setViewRoot(originalViewRoot);
                if (viewMap != null)
                {
                    originalViewRoot.getViewMap().putAll(viewMap);
                }
            }
        }

        return result;
    }

    protected void broadcastAccessDeniedException(ErrorViewAwareAccessDeniedException accessDeniedException)
    {
        AccessDeniedExceptionBroadcaster exceptionBroadcaster =
            BeanProvider.getContextualReference(AccessDeniedExceptionBroadcaster.class);

        Throwable broadcastResult = exceptionBroadcaster.broadcastAccessDeniedException(accessDeniedException);

        if (broadcastResult != null)
        {
            throw ExceptionUtils.throwAsRuntimeException(broadcastResult);
        }
    }

    private synchronized void lazyInit()
    {
        if (this.securityModuleActivated != null)
        {
            return;
        }

        this.securityModuleActivated =
            BeanProvider.getContextualReference(EditableAccessDecisionVoterContext.class, true) != null;

        if (!this.securityModuleActivated)
        {
            Logger.getLogger(getClass().getName()) //it's the only case for which a logger is needed in this class
                    .info("security-module-impl isn't used -> " + getClass().getName() + " gets deactivated");
        }
    }

    private void deactivatePreDestroyViewMapEvent(FacesContext facesContext)
    {
        facesContext.getExternalContext().getRequestMap().put(PRE_DESTROY_VIEW_MAP_EVENT_FILTER_ENABLED, Boolean.TRUE);
    }

    private void activatePreDestroyViewMapEvent(FacesContext facesContext)
    {
        facesContext.getExternalContext().getRequestMap().put(PRE_DESTROY_VIEW_MAP_EVENT_FILTER_ENABLED, Boolean.FALSE);
    }
}
