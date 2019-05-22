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

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindow;

@FacesRenderer(componentFamily = DisableClientWindowComponent.COMPONENT_FAMILY,
        rendererType = DisableClientWindowComponent.COMPONENT_TYPE)
public class DisableClientWindowHtmlRenderer extends Renderer
{
    private volatile Boolean initialized;

    private ClientWindow clientWindow;
    private JsfModuleConfig jsfModuleConfig;
    
    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException
    {
        lazyInit();
        
        boolean dsClientWindowRenderModeEnabled = clientWindow.isClientWindowRenderModeEnabled(context);
        boolean jsfClientWindowRenderModeEnabled = false;
        
        if (jsfModuleConfig.isJsf22Available())
        {
            if (context.getExternalContext().getClientWindow() != null)
            {
                jsfClientWindowRenderModeEnabled =
                        context.getExternalContext().getClientWindow().isClientWindowRenderModeEnabled(context);
            }
        }
        
        try
        {
            if (dsClientWindowRenderModeEnabled)
            {
                clientWindow.disableClientWindowRenderMode(context);
            }
            if (jsfClientWindowRenderModeEnabled)
            {
                context.getExternalContext().getClientWindow().disableClientWindowRenderMode(context);
            }

            super.encodeChildren(context, component);
        }
        finally
        {
            if (dsClientWindowRenderModeEnabled)
            {
                clientWindow.enableClientWindowRenderMode(context);
            }
            if (jsfClientWindowRenderModeEnabled)
            {
                context.getExternalContext().getClientWindow().enableClientWindowRenderMode(context);
            }
        }
    }
    
    @Override
    public boolean getRendersChildren()
    {
        return true;
    }
    
    private void lazyInit()
    {
        if (this.initialized == null)
        {
            init();
        }
    }

    protected synchronized void init()
    {
        if (this.initialized == null)
        {
            clientWindow = BeanProvider.getContextualReference(ClientWindow.class);
            jsfModuleConfig = BeanProvider.getContextualReference(JsfModuleConfig.class);
            
            this.initialized = true;
        }
    }
}
