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
package org.apache.deltaspike.core.impl.scope.conversation;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.impl.scope.DeltaSpikeContextExtension;
import org.apache.deltaspike.core.spi.scope.conversation.GroupedConversationManager;
import org.apache.deltaspike.core.util.context.ContextualStorage;

import javax.enterprise.inject.Typed;
import java.lang.annotation.Annotation;
import java.util.Set;

@Typed()
class InjectableGroupedConversationManager implements GroupedConversationManager
{
    private transient volatile GroupedConversationManager conversationManager;

    InjectableGroupedConversationManager(GroupedConversationManager conversationManager)
    {
        this.conversationManager = conversationManager;
    }

    private GroupedConversationManager getConversationManager()
    {
        if (this.conversationManager == null)
        {
            this.conversationManager =
                BeanProvider.getContextualReference(DeltaSpikeContextExtension.class).getConversationContext();
        }
        return conversationManager;
    }

    @Override
    public ContextualStorage closeConversation(Class<?> conversationGroup, Annotation... qualifiers)
    {
        return getConversationManager().closeConversation(conversationGroup, qualifiers);
    }

    @Override
    public Set<ContextualStorage> closeConversationGroup(Class<?> conversationGroup)
    {
        return getConversationManager().closeConversationGroup(conversationGroup);
    }

    @Override
    public void closeConversations()
    {
        getConversationManager().closeConversations();
    }
}
