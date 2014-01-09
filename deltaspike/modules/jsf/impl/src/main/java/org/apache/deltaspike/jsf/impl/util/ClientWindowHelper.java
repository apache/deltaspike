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
package org.apache.deltaspike.jsf.impl.util;

import java.util.Map;
import java.util.Map.Entry;
import javax.enterprise.inject.Typed;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindow;

@Typed()
public abstract class ClientWindowHelper
{
    /**
     * Handles the initial redirect for the URL modus, if no windowId is available in the current request URL.
     *
     * @param facesContext the {@link FacesContext}
     */
    public static void handleInitialRedirect(FacesContext facesContext)
    {
        ExternalContext externalContext = facesContext.getExternalContext();

        // send initial redirect to add the windowId to the current request URL
        String viewId = facesContext.getApplication().getViewHandler().deriveViewId(
                facesContext, externalContext.getRequestServletPath());

        // The NavigationHandler tries to access the UIViewRoot but it isn't available because our
        // ClientWindow will be initialized before the normal JSF lifecycle
        UIViewRoot viewRoot = new UIViewRoot();
        viewRoot.setViewId(viewId);
        facesContext.setViewRoot(viewRoot);

        String outcome = viewId + "?faces-redirect=true&includeViewParams=true";
        // append it manually - includeViewParams doesn't work here because of the not fully initialized UIViewRoot
        outcome = JsfUtils.addRequestParameters(externalContext, outcome, true);

        facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, null, outcome);
    }

    /**
     * Appends the current windowId to the given url, if enabled via
     * {@link ClientWindow#isClientWindowRenderModeEnabled(javax.faces.context.FacesContext)}
     *
     * @param facesContext the {@link FacesContext}
     * @param url the url
     * @param clientWindow the {@link ClientWindow} to use
     * @return if enabled, the url with windowId, otherwise the umodified url
     */
    public static String appendWindowId(FacesContext facesContext, String url, ClientWindow clientWindow)
    {
        if (clientWindow != null && clientWindow.isClientWindowRenderModeEnabled(facesContext))
        {
            Map<String, String> parameters = clientWindow.getQueryURLParameters(facesContext);

            if (parameters != null && !parameters.isEmpty())
            {
                String targetUrl = url;

                for (Entry<String, String> entry : parameters.entrySet())
                {
                    // NOTE: each call will instantiate a new StringBuilder
                    // i didn't optimized this call because it's unlikely that there will be multiple parameters
                    targetUrl = JsfUtils.addParameter(facesContext.getExternalContext(),
                            targetUrl,
                            true,
                            entry.getKey(),
                            entry.getValue());
                }

                return targetUrl;
            }
        }

        return url;
    }
}
