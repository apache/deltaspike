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

import org.apache.deltaspike.core.api.scope.GroupedConversation;
import org.apache.deltaspike.core.impl.scope.DeltaSpikeContextExtension;
import org.apache.deltaspike.core.impl.util.ConversationUtils;
import org.apache.deltaspike.core.spi.scope.conversation.GroupedConversationManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

@ApplicationScoped
public class GroupedConversationArtifactProducer
{
    @Inject
    private DeltaSpikeContextExtension deltaSpikeContextExtension;

    @Produces
    @Dependent
    public GroupedConversationManager getGroupedConversationManager()
    {
        return new InjectableGroupedConversationManager(deltaSpikeContextExtension.getConversationContext());
    }

    @Produces
    @Dependent
    public GroupedConversation getGroupedConversation(InjectionPoint injectionPoint, BeanManager beanManager)
    {
        ConversationKey conversationKey =
            ConversationUtils.convertToConversationKey(injectionPoint.getBean(), beanManager);
        return new InjectableGroupedConversation(conversationKey, getGroupedConversationManager());
    }
}
