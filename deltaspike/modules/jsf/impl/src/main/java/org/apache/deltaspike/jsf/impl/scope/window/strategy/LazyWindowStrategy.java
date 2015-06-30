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
package org.apache.deltaspike.jsf.impl.scope.window.strategy;

import org.apache.deltaspike.jsf.impl.util.ClientWindowHelper;

import javax.faces.context.FacesContext;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Typed;
import org.apache.deltaspike.core.util.StringUtils;

@Dependent
@Typed(LazyWindowStrategy.class)
public class LazyWindowStrategy extends AbstractClientWindowStrategy
{
    @Override
    protected String getOrCreateWindowId(FacesContext facesContext)
    {
        String windowId = ClientWindowHelper.getInitialRedirectWindowId(facesContext);

        if (StringUtils.isEmpty(windowId))
        {
            windowId = getWindowIdParameter(facesContext);
        }

        boolean post = isPost(facesContext);

        if (StringUtils.isEmpty(windowId) && post)
        {
            windowId = getWindowIdPostParameter(facesContext);
        }

        if (StringUtils.isEmpty(windowId))
        {
            if (jsfModuleConfig.isInitialRedirectEnabled() && !post)
            {
                ClientWindowHelper.handleInitialRedirect(facesContext, generateNewWindowId());
                facesContext.responseComplete();
                windowId = null;
            }
            else
            {
                windowId = generateNewWindowId();
            }
        }

        return windowId;
    }

    @Override
    protected Map<String, String> createQueryURLParameters(FacesContext facesContext)
    {
        String windowId = getWindowId(facesContext);

        if (windowId == null)
        {
            return null;
        }

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(ClientWindowHelper.RequestParameters.GET_WINDOW_ID, windowId);
        return parameters;
    }
    
    @Override
    protected boolean isSupportClientWindowRenderingMode()
    {
        return true;
    }

    @Override
    public boolean isInitialRedirectSupported(FacesContext facesContext)
    {
        return true;
    }
}
