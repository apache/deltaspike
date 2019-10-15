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

import java.util.Collections;
import java.util.Map;
import java.util.Random;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.deltaspike.core.util.StringUtils;
import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;
import org.apache.deltaspike.jsf.impl.util.ClientWindowHelper;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindow;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig;

public abstract class AbstractClientWindowStrategy implements ClientWindow
{
    /**
     * This windowId will be used for all requests with disabled windowId feature
     */
    public static final String DEFAULT_WINDOW_ID = "default";

    private static final String CACHE_QUERY_URL_PARAMETERS =
            "CACHE:" + AbstractClientWindowStrategy.class.getName() + "#getQueryURLParameters";
    private static final String CACHE_WINDOW_ID =
            "CACHE:" + AbstractClientWindowStrategy.class.getName() + ".WindowId";

    private static final String PER_USE_CLIENT_WINDOW_URL_QUERY_PARAMETER_DISABLED_KEY =
            LazyWindowStrategy.class.getName() + ".ClientWindowRenderModeEnablement";

    @Inject
    protected ClientWindowConfig clientWindowConfig;

    @Inject
    protected JsfModuleConfig jsfModuleConfig;

    private int maxWindowIdCount = 10;

    @PostConstruct
    protected void init()
    {
        this.maxWindowIdCount = ClientWindowHelper.getMaxWindowIdLength();
    }

    @Override
    public String getWindowId(FacesContext facesContext)
    {
        Map<String, Object> requestMap = facesContext.getExternalContext().getRequestMap();

        // try to lookup from cache
        String windowId = (String) requestMap.get(CACHE_WINDOW_ID);
        if (windowId != null)
        {
            return windowId;
        }

        windowId = getOrCreateWindowId(facesContext);


        if (windowId != null)
        {
            windowId = sanitiseWindowId(windowId);

            // don't cut the windowId generated from JSF
            ClientWindowConfig.ClientWindowRenderMode clientWindowRenderMode =
                    clientWindowConfig.getClientWindowRenderMode(facesContext);
            if (!ClientWindowConfig.ClientWindowRenderMode.DELEGATED.equals(clientWindowRenderMode))
            {
                if (windowId.length() > this.maxWindowIdCount)
                {
                    windowId = windowId.substring(0, this.maxWindowIdCount);
                }
            }

            requestMap.put(CACHE_WINDOW_ID, windowId);
        }

        return windowId;
    }


    /**
     * We have to escape some characters to make sure we do not open
     * any XSS vectors. E.g. replace (,<, & etc to
     * prevent attackers from injecting JavaScript function calls or html.
     */
    protected String sanitiseWindowId(String windowId)
    {
        return StringUtils.removeSpecialChars(windowId);
    }

    protected abstract String getOrCreateWindowId(FacesContext facesContext);

    protected String generateNewWindowId()
    {
        //X TODO proper mechanism
        return Integer.toString((new Random()).nextInt() % 10000);
    }

    protected String generateNewRequestToken()
    {
        return Integer.toString((int) Math.floor(Math.random() * 999));
    }
    
    protected boolean isPost(FacesContext facesContext)
    {
        if (facesContext.isPostback())
        {
            return true;
        }

        Object request = facesContext.getExternalContext().getRequest();
        if (request instanceof HttpServletRequest)
        {
            if ("POST".equals(((HttpServletRequest) request).getMethod()))
            {
                return true;
            }
        }

        return false;
    }

    protected String getWindowIdPostParameter(FacesContext facesContext)
    {
        Map<String, String> requestParams = facesContext.getExternalContext().getRequestParameterMap();
        String windowId = requestParams.get(ClientWindowHelper.RequestParameters.POST_WINDOW_ID);

        if (windowId == null)
        {
            windowId = requestParams.get(ClientWindowHelper.RequestParameters.JSF_POST_WINDOW_ID);
        }

        return windowId;
    }

    protected String getWindowIdParameter(FacesContext facesContext)
    {
        Map<String, String> requestParameters = facesContext.getExternalContext().getRequestParameterMap();
        return requestParameters.get(ClientWindowHelper.RequestParameters.GET_WINDOW_ID);
    }

    @Override
    public void disableClientWindowRenderMode(FacesContext facesContext)
    {
        if (isSupportClientWindowRenderingMode())
        {
            Map<Object, Object> attrMap = facesContext.getAttributes();
            attrMap.put(PER_USE_CLIENT_WINDOW_URL_QUERY_PARAMETER_DISABLED_KEY, Boolean.TRUE);
        }
    }

    @Override
    public void enableClientWindowRenderMode(FacesContext facesContext)
    {
        if (isSupportClientWindowRenderingMode())
        {
            Map<Object, Object> attrMap = facesContext.getAttributes();
            attrMap.remove(PER_USE_CLIENT_WINDOW_URL_QUERY_PARAMETER_DISABLED_KEY);
        }
    }

    @Override
    public boolean isClientWindowRenderModeEnabled(FacesContext facesContext)
    {
        if (isSupportClientWindowRenderingMode())
        {
            Map<Object, Object> attrMap = facesContext.getAttributes();
            return !attrMap.containsKey(PER_USE_CLIENT_WINDOW_URL_QUERY_PARAMETER_DISABLED_KEY);
        }

        return false;
    }

    protected boolean isSupportClientWindowRenderingMode()
    {
        return false;
    }

    @Override
    public boolean isInitialRedirectSupported(FacesContext facesContext)
    {
        return false;
    }

    @Override
    public Map<String, String> getQueryURLParameters(FacesContext facesContext)
    {
        Map<String, String> cachedParameters =
                (Map<String, String>) facesContext.getAttributes().get(CACHE_QUERY_URL_PARAMETERS);

        // cache paramters per request - will be called many times
        if (cachedParameters == null)
        {
            cachedParameters = createQueryURLParameters(facesContext);
            if (cachedParameters == null)
            {
                cachedParameters = Collections.EMPTY_MAP;
            }

            facesContext.getAttributes().put(CACHE_QUERY_URL_PARAMETERS, cachedParameters);
        }

        return cachedParameters;
    }

    protected Map<String, String> createQueryURLParameters(FacesContext facesContext)
    {
        return null;
    }
    
    @Override
    public String interceptRedirect(FacesContext facesContext, String url)
    {
        return ClientWindowHelper.appendWindowId(facesContext, url, this);
    }
}
