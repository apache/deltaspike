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
package org.apache.deltaspike.jsf.impl.clientwindow;

import org.apache.deltaspike.jsf.impl.util.ClientWindowHelper;

import jakarta.faces.context.FacesContext;
import java.util.Collections;
import java.util.Map;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.StringUtils;
import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;

public class LazyClientWindow extends DeltaSpikeClientWindow
{
    @Override
    public void decode(FacesContext facesContext)
    {
        id = ClientWindowHelper.getInitialRedirectWindowId(facesContext);

        if (StringUtils.isEmpty(id))
        {
            id = getWindowIdParameter(facesContext);
        }

        boolean post = isPost(facesContext);

        if (StringUtils.isEmpty(id) && post)
        {
            id = getWindowIdPostParameter(facesContext);
        }

        if (StringUtils.isEmpty(id))
        {
            id = generateNewWindowId();

            JsfModuleConfig jsfModuleConfig = BeanProvider.getContextualReference(JsfModuleConfig.class);
            if (jsfModuleConfig.isInitialRedirectEnabled() && !post)
            {
                id = sanitiseWindowId(id); // handleInitialRedirect already takes the id, just be sure and sanitise now
                ClientWindowHelper.handleInitialRedirect(facesContext, id);
                facesContext.responseComplete();
            }
        }

        id = sanitiseWindowId(id);
    }

    @Override
    public Map<String, String> getQueryURLParameters(FacesContext facesContext)
    {
        return Collections.emptyMap();
    }

    @Override
    public boolean isInitialRedirectSupported(FacesContext facesContext)
    {
        return true;
    }

    @Override
    public String interceptRedirect(FacesContext facesContext, String url)
    {
        return url;
    }
}
