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
package org.apache.deltaspike.jsf.impl.navigation;

import java.io.IOException;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialResponseWriter;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindow;

public class DeltaSpikePartialResponseWriter extends PartialResponseWriter
{
    private ClientWindow clientWindow;

    private volatile Boolean initialized;

    public DeltaSpikePartialResponseWriter(PartialResponseWriter wrapped)
    {
        super(wrapped);
    }
    
    @Override
    public void redirect(String url) throws IOException
    {
        lazyInit();

        if (clientWindow == null)
        {
            super.redirect(url);
        }
        else
        {
            super.redirect(clientWindow.interceptRedirect(FacesContext.getCurrentInstance(), url));
        }
    }
    
    private void lazyInit()
    {
        if (this.initialized == null)
        {
            init();
        }
    }

    private synchronized void init()
    {
        // switch into paranoia mode
        if (this.initialized == null)
        {
            clientWindow = BeanProvider.getContextualReference(ClientWindow.class, true);

            this.initialized = true;
        }
    }
}
