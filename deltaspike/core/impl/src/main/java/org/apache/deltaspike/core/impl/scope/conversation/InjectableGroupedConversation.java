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
import org.apache.deltaspike.core.spi.scope.conversation.GroupedConversationManager;

import javax.enterprise.inject.Typed;
import java.lang.annotation.Annotation;
import java.util.Set;

@Typed()
class InjectableGroupedConversation implements GroupedConversation
{
    private static final long serialVersionUID = -3909049219127821425L;

    private final ConversationKey conversationKey;
    private final GroupedConversationManager conversationManager;

    InjectableGroupedConversation(ConversationKey conversationKey, GroupedConversationManager conversationManager)
    {
        this.conversationManager = conversationManager;
        this.conversationKey = conversationKey;
    }

    @Override
    public void close()
    {
        Set<Annotation> qualifiers = this.conversationKey.getQualifiers();

        this.conversationManager.closeConversation(
            this.conversationKey.getConversationGroup(), qualifiers.toArray(new Annotation[qualifiers.size()]));
    }
}
