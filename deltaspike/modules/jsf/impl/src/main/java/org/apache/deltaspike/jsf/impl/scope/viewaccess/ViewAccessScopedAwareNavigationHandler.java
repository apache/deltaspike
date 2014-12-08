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
package org.apache.deltaspike.jsf.impl.scope.viewaccess;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.impl.scope.DeltaSpikeContextExtension;
import org.apache.deltaspike.core.impl.scope.viewaccess.ViewAccessContext;
import org.apache.deltaspike.core.spi.activation.Deactivatable;

public class ViewAccessScopedAwareNavigationHandler extends NavigationHandler implements Deactivatable
{
    private final NavigationHandler navigationHandler;

    private volatile Boolean initialized;
    
    private DeltaSpikeContextExtension contextExtension;
    
    public ViewAccessScopedAwareNavigationHandler(NavigationHandler navigationHandler)
    {
        this.navigationHandler = navigationHandler;
    }

    @Override
    public void handleNavigation(FacesContext context, String fromAction, String outcome)
    {
        // remember viewId before navigation
        String viewId = null;
        if (context.getViewRoot() != null)
        {
            viewId = context.getViewRoot().getViewId();
        }

        this.navigationHandler.handleNavigation(context, fromAction, outcome);
        
        if (viewId != null && context.isPostback() /*need for supporting view-actions correctly - see DELTASPIKE-795*/)
        {
            lazyInit();

            ViewAccessContext viewAccessContext = contextExtension.getViewAccessScopedContext();
            if (viewAccessContext != null)
            {
                viewAccessContext.onProcessingViewFinished(viewId);
            }
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
            contextExtension = BeanProvider.getContextualReference(DeltaSpikeContextExtension.class, true);
            
            this.initialized = true;
        }
    }
}
