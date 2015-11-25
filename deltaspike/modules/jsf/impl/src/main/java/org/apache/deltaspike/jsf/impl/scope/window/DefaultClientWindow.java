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

import org.apache.deltaspike.jsf.spi.scope.window.ClientWindow;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.util.Map;
import org.apache.deltaspike.jsf.impl.scope.window.strategy.ClientSideWindowStrategy;
import org.apache.deltaspike.jsf.impl.scope.window.strategy.DelegatedWindowStrategy;
import org.apache.deltaspike.jsf.impl.scope.window.strategy.LazyWindowStrategy;
import org.apache.deltaspike.jsf.impl.scope.window.strategy.NoneWindowStrategy;

import static org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig.ClientWindowRenderMode;

/**
 * This is the default implementation of the window/browser tab
 * detection handling for JSF applications.
 * This is to big degrees a port of Apache MyFaces CODI
 * ClientSideWindowHandler.
 *
 * It will act according to the configured {@link ClientWindowRenderMode}.
 */
@ApplicationScoped
public class DefaultClientWindow implements ClientWindow
{
    @Inject
    private ClientWindowConfig clientWindowConfig;

    @Inject
    private ClientSideWindowStrategy clientSideWindowStrategy;

    @Inject
    private DelegatedWindowStrategy delegatedWindowStrategy;

    @Inject
    private LazyWindowStrategy lazyWindowStrategy;

    @Inject
    private NoneWindowStrategy noneWindowStrategy;

    @Override
    public String getWindowId(FacesContext facesContext)
    {
        return getClientWindow(facesContext).getWindowId(facesContext);
    }

    @Override
    public void disableClientWindowRenderMode(FacesContext facesContext)
    {
        getClientWindow(facesContext).disableClientWindowRenderMode(facesContext);
    }

    @Override
    public void enableClientWindowRenderMode(FacesContext facesContext)
    {
        getClientWindow(facesContext).enableClientWindowRenderMode(facesContext);
    }

    @Override
    public boolean isClientWindowRenderModeEnabled(FacesContext facesContext)
    {
        return getClientWindow(facesContext).isClientWindowRenderModeEnabled(facesContext);
    }

    @Override
    public Map<String, String> getQueryURLParameters(FacesContext facesContext)
    {
        return getClientWindow(facesContext).getQueryURLParameters(facesContext);
    }
    
    @Override
    public boolean isInitialRedirectSupported(FacesContext facesContext)
    {
        return getClientWindow(facesContext).isInitialRedirectSupported(facesContext);
    }

    @Override
    public String interceptRedirect(FacesContext facesContext, String url)
    {
        return getClientWindow(facesContext).interceptRedirect(facesContext, url);
    }

    protected ClientWindow getClientWindow(FacesContext facesContext)
    {
        ClientWindowRenderMode clientWindowRenderMode = clientWindowConfig.getClientWindowRenderMode(facesContext);

        switch (clientWindowRenderMode)
        {
            case CLIENTWINDOW:
                return clientSideWindowStrategy;
            case CUSTOM:
                return null;
            case DELEGATED:
                return delegatedWindowStrategy;
            case LAZY:
                return lazyWindowStrategy;
            case NONE:
                return noneWindowStrategy;
            default:
                return null;
        }
    }
}
