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
package org.apache.deltaspike.jsf.impl.listener.request;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.jsf.impl.util.JsfUtils;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.ExternalContextWrapper;
import java.io.IOException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.lifecycle.ClientWindow;
import org.apache.deltaspike.jsf.impl.clientwindow.DeltaSpikeClientWindow;
import org.apache.deltaspike.jsf.impl.util.ClientWindowHelper;

public class DeltaSpikeExternalContextWrapper extends ExternalContextWrapper implements Deactivatable
{
    private final ExternalContext wrapped;

    DeltaSpikeExternalContextWrapper(ExternalContext wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public void redirect(String url) throws IOException
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        // skip if we are in initialRedirect mode because
        // save messages via flash scope will fail because the JSF lifecycle isn't initialized
        if (!ClientWindowHelper.isInitialRedirect(facesContext))
        {
            JsfUtils.saveFacesMessages(this.wrapped);
        }

        ClientWindow clientWindow = getClientWindow();
        if (clientWindow != null && clientWindow instanceof DeltaSpikeClientWindow)
        {
            url = ((DeltaSpikeClientWindow) clientWindow).interceptRedirect(facesContext, url);
        }

        this.wrapped.redirect(url);
    }

    @Override
    public ExternalContext getWrapped()
    {
        return wrapped;
    }
}
