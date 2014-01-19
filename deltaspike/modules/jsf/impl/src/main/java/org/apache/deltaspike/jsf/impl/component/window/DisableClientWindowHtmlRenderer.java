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
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindow;

@FacesRenderer(componentFamily = DisableClientWindowComponent.COMPONENT_FAMILY,
        rendererType = DisableClientWindowComponent.COMPONENT_TYPE)
public class DisableClientWindowHtmlRenderer extends Renderer
{
    private volatile ClientWindow clientWindow;
    
    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException
    {
        boolean isEnabled = getClientWindow().isClientWindowRenderModeEnabled(context);
        
        if (isEnabled)
        {
            try
            {
                getClientWindow().disableClientWindowRenderMode(context);

                super.encodeChildren(context, component);
            }
            finally
            {
                getClientWindow().enableClientWindowRenderMode(context);
            }
        }
    }
    
    @Override
    public boolean getRendersChildren()
    {
        return true;
    }
    
    private ClientWindow getClientWindow()
    {
        if (clientWindow == null)
        {
            synchronized (this)
            {
                if (clientWindow == null)
                {
                    clientWindow = BeanProvider.getContextualReference(ClientWindow.class);
                }
            }
        }

        return clientWindow;
    }
}
