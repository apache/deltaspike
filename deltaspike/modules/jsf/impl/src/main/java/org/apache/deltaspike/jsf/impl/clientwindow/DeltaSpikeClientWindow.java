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
package org.apache.deltaspike.jsf.impl.clientwindow;

import jakarta.faces.context.FacesContext;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.faces.render.ResponseStateManager;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Random;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.StringUtils;
import org.apache.deltaspike.jsf.impl.util.ClientWindowHelper;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig;

public abstract class DeltaSpikeClientWindow extends ClientWindow
{
    /**
     * This windowId will be used for all requests with disabled windowId feature
     */
    public static final String DEFAULT_WINDOW_ID = "default";
    
    private String id;
    private int maxWindowIdCount = 10;

    public DeltaSpikeClientWindow()
    {
        this.maxWindowIdCount = ClientWindowHelper.getMaxWindowIdLength();
    }
    
    @Override
    public void decode(FacesContext facesContext)
    {
        id = getOrCreateWindowId(facesContext);

        if (id != null)
        {
            id = sanitiseWindowId(id);
            if (id.length() > this.maxWindowIdCount)
            {
                id = id.substring(0, this.maxWindowIdCount);
            }
        }
    }

    @Override
    public String getId()
    {
        return id;
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
        return requestParams.get(ResponseStateManager.CLIENT_WINDOW_PARAM);
    }

    protected String getWindowIdParameter(FacesContext facesContext)
    {
        Map<String, String> requestParameters = facesContext.getExternalContext().getRequestParameterMap();
        return requestParameters.get(ResponseStateManager.CLIENT_WINDOW_URL_PARAM);
    }
    
    protected ClientWindowConfig getClientWindowConfig()
    {
        return BeanProvider.getContextualReference(ClientWindowConfig.class);
    }
    
    protected abstract String getOrCreateWindowId(FacesContext facesContext);
    
    /**
     * @return true if the implementation possible sends an initial redirect.
     */
    public abstract boolean isInitialRedirectSupported(FacesContext facesContext);
    
    /**
     * @return The new redirect url.
     */
    public abstract String interceptRedirect(FacesContext facesContext, String url);
}
