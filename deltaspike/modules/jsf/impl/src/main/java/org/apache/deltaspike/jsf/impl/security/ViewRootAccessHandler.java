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
package org.apache.deltaspike.jsf.impl.security;

import org.apache.deltaspike.core.api.config.view.metadata.ConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.jsf.impl.util.SecurityUtils;
import org.apache.deltaspike.security.spi.authorization.EditableAccessDecisionVoterContext;

import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIViewRoot;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@RequestScoped
public class ViewRootAccessHandler
{
    @Inject
    private ViewConfigResolver viewConfigResolver;

    private List<String> checkedViewIds = new ArrayList<String>();

    public void checkAccessTo(UIViewRoot uiViewRoot)
    {
        if (uiViewRoot == null)
        {
            return;
        }

        String viewId = uiViewRoot.getViewId();

        if (!checkView(viewId))
        {
            return;
        }

        this.checkedViewIds.add(viewId);

        ConfigDescriptor configDescriptor = this.viewConfigResolver.getViewConfigDescriptor(viewId);

        //topmost nodes get checked first
        Stack<ConfigDescriptor> configDescriptorStack = new Stack<ConfigDescriptor>();

        if (configDescriptor != null)
        {
            configDescriptorStack.push(configDescriptor);
        }

        List<String> parentPathList = new ArrayList<String>();
        createPathList(viewId, parentPathList);

        ConfigDescriptor pathDescriptor;
        for (String path : parentPathList)
        {
            pathDescriptor = this.viewConfigResolver.getConfigDescriptor(path);

            if (pathDescriptor != null)
            {
                configDescriptorStack.push(pathDescriptor);
            }
        }

        EditableAccessDecisionVoterContext accessDecisionVoterContext =
                BeanProvider.getContextualReference(EditableAccessDecisionVoterContext.class, false);

        for (ConfigDescriptor currentConfigDescriptor : configDescriptorStack)
        {
            SecurityUtils.invokeVoters(accessDecisionVoterContext, currentConfigDescriptor);
        }
    }

    private void createPathList(String currentPath, List<String> pathList)
    {
        if (!currentPath.contains("/"))
        {
            return;
        }

        String parentFolder = currentPath.substring(0, currentPath.lastIndexOf("/"));
        pathList.add(parentFolder + "/");
        createPathList(parentFolder, pathList);
    }

    private boolean checkView(String viewId)
    {
        return viewId != null && !this.checkedViewIds.contains(viewId);
    }
}
