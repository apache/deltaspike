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

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.apache.deltaspike.jsf.impl.util.ClientWindowHelper;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig;

@FacesRenderer(componentFamily = WindowIdComponent.COMPONENT_FAMILY, rendererType = WindowIdComponent.COMPONENT_TYPE)
@ResourceDependencies( {
        @ResourceDependency(library = "deltaspike", name = "windowhandler.js", target = "head"),
        @ResourceDependency(library = "javax.faces", name = "jsf.js", target = "head") } )
public class WindowIdHtmlRenderer extends Renderer
{
    private volatile WindowContext windowContext;
    private volatile ClientWindowConfig clientWindowConfig;

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

        String windowId = getWindowContext().getCurrentWindowId();
        String mode = getClientWindowConfig().getClientWindowRenderMode(context).name();

        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("script", component);
        writer.writeAttribute("type", "text/javascript", null);
        writer.write("window.deltaspikeWindowId='" + windowId + "';");
        writer.write("window.deltaspikeClientWindowRenderMode='" + mode + "';");
        
        // see #729
        Object cookie = ClientWindowHelper.getRequestWindowIdCookie(context, windowId);
        if (cookie != null && cookie instanceof Cookie)
        {
            Cookie servletCookie = (Cookie) cookie;
            ClientWindowHelper.removeRequestWindowIdCookie(context, servletCookie);
            writer.write("window.deltaspikeInitialRedirectWindowId='" + servletCookie.getValue() + "';");
        }

        writer.endElement("script");
    }

    private WindowContext getWindowContext()
    {
        if (windowContext == null)
        {
            synchronized (this)
            {
                if (windowContext == null)
                {
                    windowContext = BeanProvider.getContextualReference(WindowContext.class);
                }
            }
        }

        return windowContext;
    }
    
    private ClientWindowConfig getClientWindowConfig()
    {
        if (clientWindowConfig == null)
        {
            synchronized (this)
            {
                if (clientWindowConfig == null)
                {
                    clientWindowConfig = BeanProvider.getContextualReference(ClientWindowConfig.class);
                }
            }
        }

        return clientWindowConfig;
    }
}
