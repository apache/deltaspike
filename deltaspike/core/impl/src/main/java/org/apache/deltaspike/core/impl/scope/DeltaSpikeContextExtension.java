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
package org.apache.deltaspike.core.impl.scope;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.impl.scope.conversation.ConversationBeanHolder;
import org.apache.deltaspike.core.impl.scope.conversation.GroupedConversationContext;
import org.apache.deltaspike.core.impl.scope.viewaccess.ViewAccessBeanAccessHistory;
import org.apache.deltaspike.core.impl.scope.viewaccess.ViewAccessBeanHolder;
import org.apache.deltaspike.core.impl.scope.viewaccess.ViewAccessContext;
import org.apache.deltaspike.core.impl.scope.viewaccess.ViewAccessViewHistory;
import org.apache.deltaspike.core.impl.scope.window.WindowBeanHolder;
import org.apache.deltaspike.core.impl.scope.window.WindowContextImpl;
import org.apache.deltaspike.core.impl.scope.window.WindowIdHolder;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

/**
 * Handle all DeltaSpike WindowContext and ConversationContext
 * related features.
 */
public class DeltaSpikeContextExtension implements Extension, Deactivatable
{
    private WindowContextImpl windowContext;
    private GroupedConversationContext conversationContext;
    private ViewAccessContext viewAccessScopedContext;

    private Boolean isActivated = true;

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    public void registerDeltaSpikeContexts(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        if (!isActivated)
        {
            return;
        }

        windowContext = new WindowContextImpl(beanManager);
        conversationContext = new GroupedConversationContext(beanManager, windowContext);
        viewAccessScopedContext = new ViewAccessContext(beanManager, windowContext);
        afterBeanDiscovery.addContext(windowContext);
        afterBeanDiscovery.addContext(conversationContext);
        afterBeanDiscovery.addContext(viewAccessScopedContext);
    }

    /**
     * We can only initialize our contexts in AfterDeploymentValidation because
     * getBeans must not be invoked earlier than this phase to reduce randomness
     * caused by Beans no being fully registered yet.
     */
    public void initializeDeltaSpikeContexts(@Observes AfterDeploymentValidation adv, BeanManager beanManager)
    {
        if (!isActivated)
        {
            return;
        }

        WindowBeanHolder windowBeanHolder =
            BeanProvider.getContextualReference(beanManager, WindowBeanHolder.class, false);

        WindowIdHolder windowIdHolder =
            BeanProvider.getContextualReference(beanManager, WindowIdHolder.class, false);

        windowContext.init(windowBeanHolder, windowIdHolder);

        ConversationBeanHolder conversationBeanHolder =
            BeanProvider.getContextualReference(beanManager, ConversationBeanHolder.class, false);
        conversationContext.init(conversationBeanHolder);
        
        ViewAccessBeanHolder viewAccessBeanHolder =
            BeanProvider.getContextualReference(beanManager, ViewAccessBeanHolder.class, false);
        ViewAccessBeanAccessHistory viewAccessBeanAccessHistory =
            BeanProvider.getContextualReference(beanManager, ViewAccessBeanAccessHistory.class, false);
        ViewAccessViewHistory viewAccessViewHistory =
            BeanProvider.getContextualReference(beanManager, ViewAccessViewHistory.class, false);
        viewAccessScopedContext.init(viewAccessBeanHolder, viewAccessBeanAccessHistory, viewAccessViewHistory);
    }

    public WindowContextImpl getWindowContext()
    {
        return windowContext;
    }

    public GroupedConversationContext getConversationContext()
    {
        return conversationContext;
    }
    
    public ViewAccessContext getViewAccessScopedContext()
    {
        return viewAccessScopedContext;
    }
}
