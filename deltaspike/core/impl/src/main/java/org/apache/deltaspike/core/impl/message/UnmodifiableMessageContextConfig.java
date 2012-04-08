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
package org.apache.deltaspike.core.impl.message;

import org.apache.deltaspike.core.api.message.LocaleResolver;
import org.apache.deltaspike.core.api.message.MessageContext;
import org.apache.deltaspike.core.api.message.MessageInterpolator;
import org.apache.deltaspike.core.api.message.MessageResolver;

import javax.enterprise.inject.Typed;

/**
 * generated
 */
@Typed()
class UnmodifiableMessageContextConfig implements MessageContext.Config
{
    private MessageContext.Config messageContextConfig;

    UnmodifiableMessageContextConfig(MessageContext.Config messageContextConfig)
    {
        this.messageContextConfig = messageContextConfig;
    }

    @Override
    public MessageContextBuilder use()
    {
        //it's ok to delegate - the call of #use creates a new instance of the context - the old context is untouched
        return this.messageContextConfig.use();
    }


    @Override
    public MessageContextBuilder change()
    {
        throw new IllegalStateException(MessageContext.Config.class.getName() +
                "is readonly after the call of MessageContext#message");
    }

    /*
     * generated
     */

    @Override
    public MessageInterpolator getMessageInterpolator()
    {
        return messageContextConfig.getMessageInterpolator();
    }

    @Override
    public MessageResolver getMessageResolver()
    {
        return messageContextConfig.getMessageResolver();
    }

    @Override
    public LocaleResolver getLocaleResolver()
    {
        return messageContextConfig.getLocaleResolver();
    }
}
