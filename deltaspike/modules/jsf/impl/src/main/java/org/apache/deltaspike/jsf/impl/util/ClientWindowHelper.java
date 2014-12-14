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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Typed;
import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.deltaspike.jsf.api.config.base.JsfBaseConfig;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindow;

@Typed()
public abstract class ClientWindowHelper
{
    public static final String INITIAL_REDIRECT_WINDOW_ID = ClientWindowHelper.class.getName()
            + ".INITIAL_REDIRECT_WINDOW_ID";
    public static final String REQUEST_WINDOW_ID_COOKIE_PREFIX = "dsrwid-";

    private static final Logger LOG = Logger.getLogger(ClientWindowHelper.class.getName());

    /**
     * Handles the initial redirect for the URL modus, if no windowId is available in the current request URL.
     *
     * @param facesContext the {@link FacesContext}
     * @param newWindowId the new windowId
     */
    public static void handleInitialRedirect(FacesContext facesContext, String newWindowId)
    {
        // store the new windowId as context attribute to prevent infinite loops
        // #sendRedirect will append the windowId (from ClientWindow#getWindowId again) to the redirectUrl
        facesContext.getAttributes().put(INITIAL_REDIRECT_WINDOW_ID, newWindowId);

        ExternalContext externalContext = facesContext.getExternalContext();

        String url = externalContext.getRequestContextPath()
                + externalContext.getRequestServletPath();

        if (externalContext.getRequestPathInfo() != null)
        {
            url += externalContext.getRequestPathInfo();
        }
  
        url = JsfUtils.addRequestParameters(externalContext, url, true);
        //TODO check if it isn't better to fix addRequestParameters itself
        //only #encodeResourceURL is portable currently
        url = facesContext.getExternalContext().encodeResourceURL(url);

        // see #729
        addRequestWindowIdCookie(facesContext, newWindowId);

        try
        {
            externalContext.redirect(url);
        }
        catch (IOException e)
        {
            throw new FacesException("Could not send initial redirect!", e);
        }
    }

    public static boolean isInitialRedirect(FacesContext facesContext)
    {
        return facesContext.getAttributes().containsKey(INITIAL_REDIRECT_WINDOW_ID);
    }

    public static String getInitialRedirectWindowId(FacesContext facesContext)
    {
        return (String) facesContext.getAttributes().get(INITIAL_REDIRECT_WINDOW_ID);
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
                    targetUrl = JsfUtils.addParameter(facesContext.getExternalContext(),
                            targetUrl,
                            true,
                            entry.getKey(),
                            entry.getValue());
                }

                if (targetUrl.contains("dswid=&"))
                {
                    //remove empty dswid parameter
                    targetUrl = targetUrl.replace("dswid=&", "");
                }
                return targetUrl;
            }
        }

        return url;
    }
    
    public static void addRequestWindowIdCookie(FacesContext context, String windowId)
    {
        Map<String, Object> properties = new HashMap();
        properties.put("path", "/");
        properties.put("maxAge", 30);

        context.getExternalContext().addResponseCookie(
                REQUEST_WINDOW_ID_COOKIE_PREFIX + windowId, windowId, properties);
    }
    
    public static Object getRequestWindowIdCookie(FacesContext context, String windowId)
    {
        Map<String, Object> cookieMap = context.getExternalContext().getRequestCookieMap();
        
        if (cookieMap.containsKey(REQUEST_WINDOW_ID_COOKIE_PREFIX + windowId))
        {
            return cookieMap.get(REQUEST_WINDOW_ID_COOKIE_PREFIX + windowId);
        }
        
        return null;
    }
    
    public static void removeRequestWindowIdCookie(FacesContext context, Cookie cookie)
    {
        cookie.setMaxAge(0);
        ((HttpServletResponse) context.getExternalContext().getResponse()).addCookie(cookie);
    }

    public static int getMaxWindowIdLength()
    {
        int result = JsfBaseConfig.Scope.Window.ID_MAX_LENGTH.getValue();

        if (result > JsfBaseConfig.Scope.Window.ID_MAX_LENGTH.getDefaultValue())
        {
            if (LOG.isLoggable(Level.WARNING))
            {
                LOG.warning("ATTENTION: if you change this value to be significant longer than 10, " +
                    "you can introduce a security issue in WindowIdHtmlRenderer. " +
                    "If you increase it because window.name contains a value already, " +
                    "please revisit that usage or " +
                    "create shorter unique ids since they just need to be unique within the user-session.");
            }
        }
        return result;
    }
}
