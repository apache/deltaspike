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
public abstract class ClientWindow
{
    private static final String PER_USE_CLIENT_WINDOW_URL_QUERY_PARAMETER_DISABLED_KEY =
            ClientWindow.class.getName() + ".ClientWindowRenderModeEnablement";

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
    public abstract String getWindowId(FacesContext facesContext);

    /**
     * <p>Components that permit per-use disabling
     * of the appending of the ClientWindow in generated URLs must call this method
     * first before rendering those URLs.  The caller must call
     * {@link #enableClientWindowRenderMode(javax.faces.context.FacesContext)}
     * from a <code>finally</code> block after rendering the URL.  If
     * {@link #CLIENT_WINDOW_MODE_PARAM_NAME} is "url" without the quotes, all generated
     * URLs that cause a GET request must append the ClientWindow by default.
     * This is specified as a static method because callsites need to access it
     * without having access to an actual {@code ClientWindow} instance.</p>
     *
     * @param context the {@link FacesContext} for this request.
     */
    public void disableClientWindowRenderMode(FacesContext context)
    {
        Map<Object, Object> attrMap = context.getAttributes();
        attrMap.put(PER_USE_CLIENT_WINDOW_URL_QUERY_PARAMETER_DISABLED_KEY, Boolean.TRUE);
    }

    /**
     * <p>Components that permit per-use disabling
     * of the appending of the ClientWindow in generated URLs must call this method
     * first after rendering those URLs.  If
     * {@link #CLIENT_WINDOW_MODE_PARAM_NAME} is "url" without the quotes, all generated
     * URLs that cause a GET request must append the ClientWindow by default.
     * This is specified as a static method because callsites need to access it
     * without having access to an actual {@code ClientWindow} instance.</p>
     *
     * @param context the {@link FacesContext} for this request.
     */
    public void enableClientWindowRenderMode(FacesContext context)
    {
        Map<Object, Object> attrMap = context.getAttributes();
        attrMap.remove(PER_USE_CLIENT_WINDOW_URL_QUERY_PARAMETER_DISABLED_KEY);
    }

    /**
     * <p>Methods that append the ClientWindow to generated
     * URLs must call this method to see if they are permitted to do so.  If
     * {@link #CLIENT_WINDOW_MODE_PARAM_NAME} is "url" without the quotes, all generated
     * URLs that cause a GET request must append the ClientWindow by default.
     * This is specified as a static method because callsites need to access it
     * without having access to an actual {@code ClientWindow} instance.</p>
     *
     * @param context the {@link FacesContext} for this request.
     */
    public boolean isClientWindowRenderModeEnabled(FacesContext context)
    {
        Map<Object, Object> attrMap = context.getAttributes();
        boolean result = !attrMap.containsKey(PER_USE_CLIENT_WINDOW_URL_QUERY_PARAMETER_DISABLED_KEY);
        return result;
    }

    /**
     * <p>This method will be called whenever a URL
     * is generated by the runtime where client window related parameters need
     * to be inserted into the URL.  This guarantees custom {@code ClientWindow} implementations
     * that they will have the opportunity to insert any additional client window specific
     * information in any case where a URL is generated, such as the rendering
     * of hyperlinks.  The returned map must be immutable.  The default implementation of this method returns
     * the empty map.</p>

     * @param context the {@code FacesContext} for this request.
     * @return {@code null} or a map of parameters to insert into the URL query string.
     */
    public abstract Map<String, String> getQueryURLParameters(FacesContext context);
}
