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
package org.apache.deltaspike.jsf.impl.scope.window.strategy;

import java.io.IOException;
import java.io.OutputStream;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Typed;
import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.apache.deltaspike.jsf.impl.util.ClientWindowHelper;
import org.apache.deltaspike.jsf.impl.util.JsfUtils;

@Dependent
@Typed(ClientSideWindowStrategy.class)
public class ClientSideWindowStrategy extends AbstractClientWindowStrategy
{
    /**
     * Value which can be used as "window-id" by external clients which aren't aware of windows.
     * It deactivates e.g. the redirect for the initial request.
     */
    private static final String AUTOMATED_ENTRY_POINT_PARAMETER_KEY = "automatedEntryPoint";

    private static final String UNINITIALIZED_WINDOW_ID_VALUE = "uninitializedWindowId";
    private static final String WINDOW_ID_REPLACE_PATTERN = "$$windowIdValue$$";
    private static final String REQUEST_URL_REPLACE_PATTERN = "$$requestUrl$$";
    private static final String NOSCRIPT_URL_REPLACE_PATTERN = "$$noscriptUrl$$";

    /**
     * Use this parameter to force a 'direct' request from the clients without any windowId detection
     * We keep this name for backward compat with CODI.
     */
    private static final String NOSCRIPT_PARAMETER = "mfDirect";


    @Override
    protected String getOrCreateWindowId(FacesContext facesContext)
    {
        String windowId = null;

        boolean post = isPost(facesContext);

        if (post)
        {
            windowId = getWindowIdPostParameter(facesContext);
        }
        else if (isNoscriptRequest(facesContext.getExternalContext()))
        {
            // the client has JavaScript disabled
            clientWindowConfig.setJavaScriptEnabled(false);

            windowId = DEFAULT_WINDOW_ID;
        }
        else
        {
            windowId = getVerifiedWindowIdFromCookie(facesContext.getExternalContext());

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
                sendWindowHandlerHtml(facesContext.getExternalContext(), windowId);
                facesContext.responseComplete();
            }
        }

        return windowId;
    }

    protected boolean isNoscriptRequest(ExternalContext externalContext)
    {
        String noscript = externalContext.getRequestParameterMap().get(NOSCRIPT_PARAMETER);

        return (noscript != null && "true".equals(noscript));
    }

    protected void sendWindowHandlerHtml(ExternalContext externalContext, String windowId)
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
            windowHandlerHtml = windowHandlerHtml.replace(WINDOW_ID_REPLACE_PATTERN,
                                                          org.owasp.encoder.Encode.forJavaScriptBlock(windowId));
            // set the current request url
            // on the client we can't use window.location as the location
            // could be a different when using forwards
            windowHandlerHtml = windowHandlerHtml.replace(REQUEST_URL_REPLACE_PATTERN,
                                                          org.owasp.encoder.Encode.forJavaScriptBlock(
                                                              ClientWindowHelper.constructRequestUrl(externalContext)));
            // set the noscript-URL for users with no JavaScript
            windowHandlerHtml =
                windowHandlerHtml.replace(NOSCRIPT_URL_REPLACE_PATTERN,
                                          org.owasp.encoder.Encode.forHtmlAttribute(getNoscriptUrl(externalContext)));

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

    protected String getNoscriptUrl(ExternalContext externalContext)
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
        url = JsfUtils.addParameter(externalContext, url, false, NOSCRIPT_PARAMETER, "true");

        // NOTE that the url could contain data for an XSS attack
        // like e.g. ?"></a><a href%3D"http://hacker.org/attack.html?a
        // DO NOT REMOVE THE FOLLOWING LINES!
        url = url.replace("\"", "");
        url = url.replace("\'", "");

        return url;
    }

    protected String getVerifiedWindowIdFromCookie(ExternalContext externalContext)
    {
        String cookieName =
                ClientWindowHelper.Cookies.REQUEST_WINDOW_ID_PREFIX + getRequestTokenParameter(externalContext);
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

    protected String getRequestTokenParameter(ExternalContext externalContext)
    {
        String requestToken =
                externalContext.getRequestParameterMap().get(ClientWindowHelper.RequestParameters.REQUEST_TOKEN);
        if (requestToken != null)
        {
            return requestToken;
        }

        return "";
    }

    @Override
    public String interceptRedirect(FacesContext facesContext, String url)
    {
        // following cases we can mark as valid next request:
        // 1) request == !ajax and GET
        //   A redirect via ExternalContext can only be done in a JSF request.
        //   As the windowId is validated before the JSF lifecycle starts
        //   (via windowhandler streaming/request token validation), we can assume that the current request
        //   is valid and we can just mark the next request/redirect as valid, too.
        // 2) request == ajax and POST
        //   Ajax is always a "post back", so the browser tab was already validated in earlier requests.
        //   
        //   
        // following cases we can NOT mark as valid next request:
        // 1) request == !ajax and POST
        //   This is a Post/Redirect/Get - as the post can be done to a new browser tab
        //   (via the target attribute on the form), the windowId must NOT be valid.
        // 2) request == ajax and GET
        //   Not a common JSF request.
        //   
        boolean ajax = facesContext.getPartialViewContext().isAjaxRequest();
        boolean post = isPost(facesContext);
        boolean get = !post;
        if ((!ajax && get) || (ajax && post))
        {
            String requestToken = generateNewRequestToken();
            String windowId = getWindowId(facesContext);
            
            ClientWindowHelper.addRequestWindowIdCookie(facesContext,
                    requestToken,
                    windowId);

            url = JsfUtils.addParameter(facesContext.getExternalContext(),
                    url,
                    true,
                    ClientWindowHelper.RequestParameters.GET_WINDOW_ID,
                    windowId);
            url = JsfUtils.addParameter(facesContext.getExternalContext(),
                    url,
                    true,
                    ClientWindowHelper.RequestParameters.REQUEST_TOKEN,
                    requestToken);

            return url;
        }
        
        return url;
    }
}
