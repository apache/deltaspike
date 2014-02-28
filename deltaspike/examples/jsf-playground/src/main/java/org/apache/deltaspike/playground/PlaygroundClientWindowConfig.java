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
package org.apache.deltaspike.playground;

import javax.enterprise.inject.Specializes;
import javax.faces.context.FacesContext;
import org.apache.deltaspike.jsf.spi.scope.window.DefaultClientWindowConfig;

@Specializes
public class PlaygroundClientWindowConfig extends DefaultClientWindowConfig
{
    @Override
    public ClientWindowRenderMode getClientWindowRenderMode(FacesContext facesContext)
    {
        String path = facesContext.getExternalContext().getRequestPathInfo();
        if (path == null)
        {
            path = facesContext.getExternalContext().getRequestServletPath();
        }

        ClientWindowRenderMode mode;

        if (path.contains("/windowhandling/clientwindow/"))
        {
            mode = ClientWindowRenderMode.CLIENTWINDOW;
        }
        else if (path.contains("/windowhandling/lazy/"))
        {
            mode = ClientWindowRenderMode.LAZY;
        }
        else if (path.contains("/windowhandling/none/"))
        {
            mode = ClientWindowRenderMode.NONE;
        }
        else if (path.contains("/windowhandling/delegated/"))
        {
            mode = ClientWindowRenderMode.DELEGATED;
        }
        else
        {
            mode = ClientWindowRenderMode.CLIENTWINDOW;
        }

        return mode;
    }
}
