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
package org.apache.deltaspike.jsf.impl.component.window;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.jsf.impl.util.ClientWindowHelper;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindow;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig;

@FacesRenderer(componentFamily = WindowIdComponent.COMPONENT_FAMILY, rendererType = WindowIdComponent.COMPONENT_TYPE)
@ResourceDependencies( {
        @ResourceDependency(library = "deltaspike", name = "windowhandler.js", target = "head"),
        @ResourceDependency(library = "javax.faces", name = "jsf.js", target = "head") } )
public class WindowIdHtmlRenderer extends Renderer
{
    private volatile ClientWindow clientWindow;
    private volatile ClientWindowConfig clientWindowConfig;
    private int maxWindowIdLength = 10;

    /**
     * 'deltaspikeJsWindowId' will be used to:
     * Write a simple hidden field into the form.
     * This might change in the future...
     * @param context
     * @param component
     * @throws IOException
     */
    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException
    {
        super.encodeBegin(context, component);

        lazyInit();

        ClientWindowConfig.ClientWindowRenderMode clientWindowRenderMode =
                clientWindowConfig.getClientWindowRenderMode(context);

        // see DELTASPIKE-1113
        boolean delegatedWindowMode =
            ClientWindowConfig.ClientWindowRenderMode.DELEGATED.equals(clientWindowRenderMode);
        if (delegatedWindowMode)
        {
            return;
        }

        String windowId = clientWindow.getWindowId(context);
        // just to get sure if a user provides a own client window
        windowId = secureWindowId(windowId);

        ResponseWriter writer = context.getResponseWriter();
        writer.write("<script type=\"text/javascript\">");
        writer.write("(function(){");
        writer.write("dswh.init('");
        writer.writeText(windowId, null);
        writer.write("','"
                + clientWindowRenderMode.name() + "',"
                + maxWindowIdLength + ",{");

        writer.write("'tokenizedRedirect':" + clientWindowConfig.isClientWindowTokenizedRedirectEnabled());
        writer.write(",'storeWindowTreeOnLinkClick':"
                + clientWindowConfig.isClientWindowStoreWindowTreeEnabledOnLinkClick());
        writer.write(",'storeWindowTreeOnButtonClick':"
                + clientWindowConfig.isClientWindowStoreWindowTreeEnabledOnButtonClick());

        // see #729
        if (clientWindow.isInitialRedirectSupported(context))
        {
            Object cookie = ClientWindowHelper.getRequestWindowIdCookie(context, windowId);
            if (cookie != null && cookie instanceof Cookie)
            {
                Cookie servletCookie = (Cookie) cookie;
                writer.write(",'initialRedirectWindowId':'" + secureWindowId(servletCookie.getValue()) + "'");
                // expire/remove cookie
                servletCookie.setMaxAge(0);
                ((HttpServletResponse) context.getExternalContext().getResponse()).addCookie(servletCookie);
            }
        }

        writer.write("});");
        writer.write("})();");
        writer.write("</script>");
    }

    protected String secureWindowId(String windowId)
    {
        //restrict the length to prevent script-injection
        if (windowId != null && windowId.length() > this.maxWindowIdLength)
        {
            windowId = windowId.substring(0, this.maxWindowIdLength);
        }
        return windowId;
    }

    private void lazyInit()
    {
        if (clientWindow == null)
        {
            synchronized (this)
            {
                if (clientWindow == null)
                {
                    clientWindowConfig = BeanProvider.getContextualReference(ClientWindowConfig.class);
                    maxWindowIdLength = ClientWindowHelper.getMaxWindowIdLength();

                    clientWindow = BeanProvider.getContextualReference(ClientWindow.class);
                }
            }
        }
    }
}
