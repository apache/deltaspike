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

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitWrapper;
import java.io.Writer;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;

/**
 * Wraps the RenderKit and adds the
 * {@link WindowIdHolderComponent} to the view tree
 */
public class WindowIdRenderKitWrapper extends RenderKitWrapper
{
    private final RenderKit wrapped;

    /**
     * This will get initialized lazily to prevent boot order issues
     * with the JSF and CDI containers.
     */
    private volatile WindowContext windowContext;


    //needed if the renderkit gets proxied - see EXTCDI-215
    protected WindowIdRenderKitWrapper()
    {
        this.wrapped = null;
    }

    public WindowIdRenderKitWrapper(RenderKit wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public RenderKit getWrapped()
    {
        return wrapped;
    }

    /**
     * Adds a {@link WindowIdHolderComponent} with the
     * current windowId to the component tree.
     */
    public ResponseWriter createResponseWriter(Writer writer, String s, String s1)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String windowId = getWindowContext().getCurrentWindowId();

        WindowIdHolderComponent.addWindowIdHolderComponent(facesContext, windowId);

        return wrapped.createResponseWriter(writer, s, s1);
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
}
