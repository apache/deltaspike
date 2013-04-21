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
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import java.util.logging.Logger;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
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
    private static final Logger logger = Logger.getLogger(DefaultClientWindow.class.getName());


    @Inject
    private ClientWindowConfig clientWindowConfig;

    @Inject
    private WindowContext windowContext;


    @Override
    public String getWindowId(FacesContext facesContext)
    {
        if (ClientWindowRenderMode.NONE.equals(clientWindowConfig.getClientWindowRenderMode(facesContext)))
        {
            return null;
        }

        String windowId = null;

        if (facesContext.isPostback())
        {
            return getPostBackWindowId(facesContext);
        }

        return windowId;
    }

    /**
     * Extract the windowId for http POST
     */
    private String getPostBackWindowId(FacesContext facesContext)
    {
        UIViewRoot uiViewRoot = facesContext.getViewRoot();

        if (uiViewRoot != null)
        {
            WindowIdHolderComponent existingWindowIdHolder
                = WindowIdHolderComponent.getWindowIdHolderComponent(uiViewRoot);
            if (existingWindowIdHolder != null)
            {
                return existingWindowIdHolder.getWindowId();
            }
        }

        return null;
    }


}
