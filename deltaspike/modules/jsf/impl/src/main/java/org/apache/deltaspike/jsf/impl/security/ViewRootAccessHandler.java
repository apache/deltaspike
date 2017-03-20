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

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.jsf.api.security.ViewAccessHandler;
import org.apache.deltaspike.jsf.impl.util.SecurityUtils;
import org.apache.deltaspike.security.api.authorization.ErrorViewAwareAccessDeniedException;
import org.apache.deltaspike.security.spi.authorization.EditableAccessDecisionVoterContext;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIViewRoot;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class ViewRootAccessHandler implements ViewAccessHandler
{
    @Inject
    private ViewConfigResolver viewConfigResolver;
    
    private ConcurrentMap<String, List<ConfigDescriptor<?>>> descriptorStacks =
            new ConcurrentHashMap<String, List<ConfigDescriptor<?>>>();

    @Override
    public boolean canAccessView(String viewId)
    {
        try
        {
            checkAccessToView(viewId);
            return true;
        }
        catch (ErrorViewAwareAccessDeniedException e)
        {
            return false;
        }
    }

    @Override
    public boolean canAccessView(UIViewRoot viewRoot)
    {
        try
        {
            checkAccessToView(viewRoot);
            return true;
        }
        catch (ErrorViewAwareAccessDeniedException e)
        {
            return false;
        }
    }

    @Override
    public boolean canAccessView(ViewConfigDescriptor viewDescriptor)
    {
        try
        {
            checkAccessToView(viewDescriptor);
            return true;
        }
        catch (ErrorViewAwareAccessDeniedException e)
        {
            return false;
        }
    }

    @Override
    public boolean canAccessView(Class<? extends ViewConfig> viewConfig)
    {
        try
        {
            checkAccessToView(viewConfig);
            return true;
        }
        catch (ErrorViewAwareAccessDeniedException e)
        {
            return false;
        }
    }
    
    @Override
    public void checkAccessToView(String viewId)
    {
        if (viewId == null)
        {
            return;
        }
        
        // check if we already know which descriptors to invoke
        List<ConfigDescriptor<?>> descriptorStack = descriptorStacks.get(viewId);
        if (descriptorStack == null)
        {
            // we don't know which descriptors to invoke
            // build the stack such that parent nodes (i.e. folders) get checked first
            descriptorStack = new ArrayList<ConfigDescriptor<?>>();
            int separatorIndex = 0;
            ConfigDescriptor<?> descriptor;
            while ((separatorIndex = viewId.indexOf('/', separatorIndex)) != -1)
            {
                descriptor = viewConfigResolver.getConfigDescriptor(viewId.substring(0, ++separatorIndex));
                if (descriptor != null)
                {
                    descriptorStack.add(descriptor);
                }
            }
            
            // add the view itself
            descriptor = viewConfigResolver.getConfigDescriptor(viewId);
            if (descriptor != null)
            {
                descriptorStack.add(descriptor);
            }
            
            // add the stack to the cache
            descriptorStacks.putIfAbsent(viewId, descriptorStack);
        }
        
        // invoke the descriptors
        EditableAccessDecisionVoterContext accessDecisionVoterContext =
                BeanProvider.getContextualReference(EditableAccessDecisionVoterContext.class, false);
        for (ConfigDescriptor<?> descriptor : descriptorStack)
        {
            SecurityUtils.invokeVoters(accessDecisionVoterContext, descriptor);
        }
    }

    @Override
    public void checkAccessToView(UIViewRoot viewRoot)
    {
        checkAccessToView(viewRoot == null ? null : viewRoot.getViewId());
    }

    @Override
    public void checkAccessToView(ViewConfigDescriptor viewDescriptor)
    {
        checkAccessToView(viewDescriptor == null ? null : viewDescriptor.getViewId());
    }

    @Override
    public void checkAccessToView(Class<? extends ViewConfig> viewConfig)
    {
        checkAccessToView(viewConfig == null ? null : viewConfigResolver.getViewConfigDescriptor(viewConfig));
    }
}
