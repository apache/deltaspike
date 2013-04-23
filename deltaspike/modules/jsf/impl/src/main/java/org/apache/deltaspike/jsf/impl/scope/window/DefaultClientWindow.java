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
package org.apache.deltaspike.jsf.impl.scope.window;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.apache.deltaspike.jsf.impl.util.JsfUtils;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindow;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig;

import static org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig.ClientWindowRenderMode;

/**
 * This is the default implementation of the window/browser tab
 * detection handling for JSF applications.
 * This is to big degrees a port of Apache MyFaces CODI
 * ClientSideWindowHandler.
 *
 * It will act according to the configured {@link ClientWindowRenderMode}.
 *
 *
 */
@ApplicationScoped
public class DefaultClientWindow implements ClientWindow
{

    /**
     * Value which can be used as "window-id" by external clients which aren't aware of windows.
     * It deactivates e.g. the redirect for the initial request.
     */
    public static final String AUTOMATED_ENTRY_POINT_PARAMETER_KEY = "automatedEntryPoint";

    /**
     * The parameter for the windowId for GET requests
     */
    public static final String DELTASPIKE_WINDOW_ID_PARAM = "windowId";

    /**
     * The parameter for the windowId for POST requests
     */
    public static final String DELTASPIKE_WINDOW_ID_POST_PARAM = "dsPostWindowId";

    private static final Logger logger = Logger.getLogger(DefaultClientWindow.class.getName());


    private static final String WINDOW_ID_COOKIE_PREFIX = "dsWindowId-";
    private static final String DELTASPIKE_REQUEST_TOKEN = "dsRid";

    private static final String UNINITIALIZED_WINDOW_ID_VALUE = "uninitializedWindowId";
    private static final String WINDOW_ID_REPLACE_PATTERN = "$$windowIdValue$$";
    private static final String NOSCRIPT_URL_REPLACE_PATTERN = "$$noscriptUrl$$";


    /**
     * Use this parameter to force a 'direct' request from the clients without any windowId detection
     * We keep this name for backward compat with CODI.
     */
    private static final String NOSCRIPT_PARAMETER = "mfDirect";

    /**
     * This windowId will be used for all requests with disabled windowId feature
     */
    private static final String DEFAULT_WINDOW_ID = "default";


    @Inject
    private ClientWindowConfig clientWindowConfig;

    @Inject
    private WindowContext windowContext;


    @Override
    public String getWindowId(FacesContext facesContext)
    {
        if (ClientWindowRenderMode.NONE.equals(clientWindowConfig.getClientWindowRenderMode(facesContext)))
        {
            // if this request should not get any window detection then we are done
            return DEFAULT_WINDOW_ID;
        }

        if (facesContext.isPostback())
        {
            // for POST we read the windowId from the WindowIdHolderComponent in our ViewRoot
            return getPostBackWindowId(facesContext);
        }

        ExternalContext externalContext = facesContext.getExternalContext();

        // and now for the GET request stuff
        if (isNoscriptRequest(externalContext))
        {
            // the client has JavaScript disabled
            clientWindowConfig.setJavaScriptEnabled(false);

            return DEFAULT_WINDOW_ID;
        }

        String windowId = getVerifiedWindowIdFromCookie(externalContext);

        boolean newWindowIdRequested = false;
        if (AUTOMATED_ENTRY_POINT_PARAMETER_KEY.equals(windowId))
        {
            // this is a marker for generating a new windowId
            windowId = generateNewWindowId();
            newWindowIdRequested = true;
        }

        if (windowId == null || newWindowIdRequested)
        {
            // GET request without windowId - send windowhandlerfilter.html to get the windowId
            sendWindowHandlerHtml(externalContext, windowId);
            facesContext.responseComplete();
        }

        // we have a valid windowId - set it and continue with the request
        return windowId;
    }

    /**
     * Create a unique windowId
     * @return
     */
    private String generateNewWindowId()
    {
        //X TODO proper mechanism
        return "" + (new Random()).nextInt() % 10000;
    }

    /**
     * Extract the windowId for http POST
     */
    private String getPostBackWindowId(FacesContext facesContext)
    {
        Map<String, String> requestParams = facesContext.getExternalContext().getRequestParameterMap();
        String windowId = requestParams.get(DELTASPIKE_WINDOW_ID_POST_PARAM);
        return windowId;
    }


    private boolean isNoscriptRequest(ExternalContext externalContext)
    {
        String noscript = externalContext.getRequestParameterMap().get(NOSCRIPT_PARAMETER);

        return (noscript != null && "true".equals(noscript));
    }


    private void sendWindowHandlerHtml(ExternalContext externalContext, String windowId)
    {
        HttpServletResponse httpResponse = (HttpServletResponse) externalContext.getResponse();

        try
        {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            httpResponse.setContentType("text/html");

            String windowHandlerHtml = clientWindowConfig.getClientWindowHtml();

            if (windowId == null)
            {
                windowId = UNINITIALIZED_WINDOW_ID_VALUE;
            }

            // set the windowId value in the javascript code
            windowHandlerHtml = windowHandlerHtml.replace(WINDOW_ID_REPLACE_PATTERN, windowId);

            // set the noscript-URL for users with no JavaScript
            windowHandlerHtml =
                    windowHandlerHtml.replace(NOSCRIPT_URL_REPLACE_PATTERN, getNoscriptUrl(externalContext));

            OutputStream os = httpResponse.getOutputStream();
            try
            {
                os.write(windowHandlerHtml.getBytes());
            }
            finally
            {
                os.close();
            }
        }
        catch (IOException ioe)
        {
            throw new FacesException(ioe);
        }
    }

    private String getNoscriptUrl(ExternalContext externalContext)
    {
        String url = externalContext.getRequestPathInfo();
        if (url == null)
        {
            url = "";
        }

        // only use the very last part of the url
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash != -1)
        {
            url = url.substring(lastSlash + 1);
        }

        // add request parameter
        url = JsfUtils.addPageParameters(externalContext, url, true);

        // add noscript parameter
        if (url.contains("?"))
        {
            url = url + "&";
        }
        else
        {
            url = url + "?";
        }
        url = url + NOSCRIPT_PARAMETER + "=true";

        // NOTE that the url could contain data for an XSS attack
        // like e.g. ?"></a><a href%3D"http://hacker.org/attack.html?a
        // DO NOT REMOVE THE FOLLOWING LINES!
        url = url.replace("\"", "");
        url = url.replace("\'", "");

        return url;
    }

    private String getVerifiedWindowIdFromCookie(ExternalContext externalContext)
    {
        String cookieName = WINDOW_ID_COOKIE_PREFIX + getRequestToken(externalContext);
        Cookie cookie = (Cookie) externalContext.getRequestCookieMap().get(cookieName);

        if (cookie != null)
        {
            // manually blast the cookie away, otherwise it pollutes the
            // cookie storage in some browsers. E.g. Firefox doesn't
            // cleanup properly, even if the max-age is reached.
            cookie.setMaxAge(0);

            return cookie.getValue();
        }

        return null;
    }

    private String getRequestToken(ExternalContext externalContext)
    {
        String requestToken = externalContext.getRequestParameterMap().get(DELTASPIKE_REQUEST_TOKEN);
        if (requestToken != null)
        {
            return requestToken;
        }

        return "";
    }

}
