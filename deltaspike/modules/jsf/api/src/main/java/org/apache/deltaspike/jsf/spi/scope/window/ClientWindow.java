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
package org.apache.deltaspike.jsf.spi.scope.window;

import java.util.Map;
import javax.faces.context.FacesContext;

/**
 * <p>API to interact with the window/browser tab handling.
 * This originally got implemented in Apache MyFaces CODI
 * which was the basis for the respective feature in JSF-2.2.
 * We now orientate us a bit on the JSF-2.2 API for making it
 * easier to provide this feature for JSF-2.0, JSF-2.1 and also
 * JSF-2.2 JSF implementations.</p>
 *
 * <p>Please not that in JSF-2.2 a <code>javax.faces.lifecycle.ClientWindow</code>
 * instance gets created for each and every request, but in DeltaSpike our
 * ClientWindow instances are most likely &#064;ApplicationScoped.
 * </p>
 */
public interface ClientWindow
{
    /**
     * Extract the windowId for the current request.
     * This method is intended to get executed at the start of the JSF lifecycle.
     * We also need to take care about JSF-2.2 ClientWindow in the future.
     * Depending on the {@link ClientWindowConfig.ClientWindowRenderMode} and
     * after consulting {@link ClientWindowConfig} we will first send an
     * intermediate page if the request is an initial GET request.
     *
     * @param facesContext for the request
     * @return the extracted WindowId of the Request, or <code>null</code> if there is no window assigned.
     */
    String getWindowId(FacesContext facesContext);

    /**
     * Can be called to disable the window-id for an URL.
     * Don't forget to call {@link #enableClientWindowRenderMode(javax.faces.context.FacesContext)}
     * after rendering the markup for the current component, if {@link #isClientWindowRenderModeEnabled}
     * returned <code>true</code> before calling this method.
     */
    void disableClientWindowRenderMode(FacesContext facesContext);

    /**
     * Can be used to reset window-id rendering for a specific component.
     */
    void enableClientWindowRenderMode(FacesContext facesContext);

    /**
     * @return true if the window-id should be appended during the rendering-process, false otherwise
     */
    boolean isClientWindowRenderModeEnabled(FacesContext facesContext);

    /**
     * @return meta-data for the current window which should get added to URLs, null otherwise
     */
    Map<String, String> getQueryURLParameters(FacesContext facesContext);
    
    /**
     * @return true if the implementation possible sends an initial redirect.
     */
    boolean isInitialRedirectSupported(FacesContext facesContext);
    
    /**
     * @return The new redirect url.
     */
    String interceptRedirect(FacesContext facesContext, String url);
}
