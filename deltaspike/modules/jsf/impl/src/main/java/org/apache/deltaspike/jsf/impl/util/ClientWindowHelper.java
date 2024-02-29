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

import jakarta.enterprise.inject.Typed;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.faces.render.ResponseStateManager;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.deltaspike.jsf.api.config.base.JsfBaseConfig;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Typed()
public abstract class ClientWindowHelper
{
    private static final Logger LOG = Logger.getLogger(ClientWindowHelper.class.getName());

    private static final String INITIAL_REDIRECT_WINDOW_ID = ClientWindowHelper.class.getName()
            + ".INITIAL_REDIRECT_WINDOW_ID";

    public abstract class RequestParameters
    {
        public static final String REQUEST_TOKEN = "dsrid";
    }

    public abstract class Cookies
    {
        public static final String REQUEST_WINDOW_ID_PREFIX = "dsrwid-";
    }

    public static String constructRequestUrl(FacesContext facesContext, ExternalContext externalContext)
    {
        String url = externalContext.getRequestContextPath()
                + externalContext.getRequestServletPath();

        if (externalContext.getRequestPathInfo() != null)
        {
            url += externalContext.getRequestPathInfo();
        }

        url = JsfUtils.addRequestParameters(externalContext, url, true);
        // always remove jfwid to force adding new jfwid as JSF impl otherwise just ignores it
        url = JsfUtils.removeUrlParameter(url, ResponseStateManager.CLIENT_WINDOW_URL_PARAM);

        // TODO currently this is broken in Mojarra and will be fixed in 4.0.6
        // url = externalContext.encodeRedirectURL(url, null);

        // let's reuse the logic from MyFaces instead
        HttpServletResponse servletResponse = (HttpServletResponse) externalContext.getResponse();
        url = servletResponse.encodeRedirectURL(encodeURL(url, facesContext, servletResponse.getCharacterEncoding()));

        return url;
    }

    // copied from MyFaces
    private static String encodeURL(String baseUrl, FacesContext facesContext, String encoding)
    {
        if (baseUrl == null)
        {
            throw new NullPointerException("baseUrl is null");
        }

        String fragment = null;
        String queryString = null;
        Map<String, List<String>> paramMap = null;

        //extract any URL fragment
        int index = baseUrl.indexOf('#');
        if (index != -1)
        {
            fragment = baseUrl.substring(index + 1);
            baseUrl = baseUrl.substring(0, index);
        }

        //extract the current query string and add the params to the paramMap
        index = baseUrl.indexOf('?');
        if (index != -1)
        {
            queryString = baseUrl.substring(index + 1);
            baseUrl = baseUrl.substring(0, index);
            String[] nameValuePairs = queryString.split("&");
            for (int i = 0; i < nameValuePairs.length; i++)
            {
                String[] currentPair = nameValuePairs[i].split("=");
                String currentName = currentPair[0];

                if (paramMap == null)
                {
                    paramMap = new HashMap<>(5, 1f);
                }

                List<String> values = paramMap.get(currentName);
                if (values == null)
                {
                    values = new ArrayList<>(1);
                    paramMap.put(currentName, values);
                }

                try
                {
                    values.add(currentPair.length > 1
                            ? URLDecoder.decode(currentPair[1], encoding)
                            : "");
                }
                catch (UnsupportedEncodingException e)
                {
                    //shouldn't ever get here
                    throw new UnsupportedOperationException("Encoding type=" + encoding
                            + " not supported", e);
                }
            }
        }

        ClientWindow window = facesContext.getExternalContext().getClientWindow();
        if (window != null && window.isClientWindowRenderModeEnabled(facesContext))
        {
            if (paramMap == null)
            {
                paramMap = new HashMap<>(5, 1f);
            }

            if (!paramMap.containsKey(ResponseStateManager.CLIENT_WINDOW_URL_PARAM))
            {
                paramMap.put(ResponseStateManager.CLIENT_WINDOW_URL_PARAM, Arrays.asList(window.getId()));
            }

            Map<String, String> additionalQueryURLParameters = window.getQueryURLParameters(facesContext);
            if (additionalQueryURLParameters != null)
            {
                for (Map.Entry<String , String> entry : additionalQueryURLParameters.entrySet())
                {
                    paramMap.put(entry.getKey(), Arrays.asList(entry.getValue()));
                }
            }
        }

        boolean hasParams = paramMap != null && !paramMap.isEmpty();

        if (!hasParams && fragment == null)
        {
            return baseUrl;
        }

        // start building the new URL
        StringBuilder newUrl = new StringBuilder(baseUrl.length() + 10);
        newUrl.append(baseUrl);

        //now add the updated param list onto the url
        if (hasParams)
        {
            boolean isFirstPair = true;
            for (Map.Entry<String, List<String>> pair : paramMap.entrySet())
            {
                for (int i = 0; i < pair.getValue().size(); i++)
                {
                    String value = pair.getValue().get(i);

                    if (!isFirstPair)
                    {
                        newUrl.append('&');
                    }
                    else
                    {
                        newUrl.append('?');
                        isFirstPair = false;
                    }

                    newUrl.append(pair.getKey());
                    newUrl.append('=');
                    if (value != null)
                    {
                        try
                        {
                            newUrl.append(URLEncoder.encode(value, encoding));
                        }
                        catch (UnsupportedEncodingException e)
                        {
                            //shouldn't ever get here
                            throw new UnsupportedOperationException("Encoding type=" + encoding
                                    + " not supported", e);
                        }
                    }
                }
            }
        }

        //add the fragment back on (if any)
        if (fragment != null)
        {
            newUrl.append('#');
            newUrl.append(fragment);
        }

        return newUrl.toString();
    }

    /**
     * Handles the initial redirect for the LAZY mode, if no windowId is available in the current request URL.
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

        String url = constructRequestUrl(facesContext, externalContext);

        // remember the initial redirect windowId till the next request - see #729
        addRequestWindowIdCookie(facesContext, newWindowId, newWindowId);

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

    public static void addRequestWindowIdCookie(FacesContext context, String requestToken, String windowId)
    {
        /* Sadly doesn't work due to SameSite is not allowed on Java cookies ^^
            Map<String, Object> properties = new HashMap();
            properties.put("path", "/");
            properties.put("maxAge", 30);
            context.getExternalContext().addResponseCookie(
                Cookies.REQUEST_WINDOW_ID_PREFIX + requestToken, windowId, properties);
        */
        context.getExternalContext().addResponseHeader("Set-Cookie",
            Cookies.REQUEST_WINDOW_ID_PREFIX + requestToken + "=" + windowId +
                "; path=/; maxAge=30; SameSite=Strict");
    }

    public static Object getRequestWindowIdCookie(FacesContext context, String requestToken)
    {
        Map<String, Object> cookieMap = context.getExternalContext().getRequestCookieMap();

        if (cookieMap.containsKey(Cookies.REQUEST_WINDOW_ID_PREFIX + requestToken))
        {
            return cookieMap.get(Cookies.REQUEST_WINDOW_ID_PREFIX + requestToken);
        }

        return null;
    }

    public static int getMaxWindowIdLength()
    {
        int result = JsfBaseConfig.ScopeCustomization.WindowRestriction.ID_MAX_LENGTH;

        if (result > JsfBaseConfig.ScopeCustomization.WindowRestriction.ID_MAX_LENGTH_DEFAULT)
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
