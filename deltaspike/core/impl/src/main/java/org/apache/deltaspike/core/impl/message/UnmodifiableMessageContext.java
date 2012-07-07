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
import java.util.Locale;

/**
 * {@link MessageContext} which doesn't support changes
 */
@Typed()
class UnmodifiableMessageContext implements MessageContext
{
    private static final long serialVersionUID = -4730350864157813259L;
    private MessageContext messageContext;

    UnmodifiableMessageContext(MessageContext messageContext)
    {
        this.messageContext = messageContext;
    }

    @Override
    public LocaleResolver getLocaleResolver()
    {
        return messageContext.getLocaleResolver();
    }

    @Override
    public MessageInterpolator getMessageInterpolator()
    {
        return messageContext.getMessageInterpolator();
    }

    @Override
    public MessageResolver getMessageResolver()
    {
        return messageContext.getMessageResolver();
    }

    @Override
    public MessageBuilder message()
    {
        return messageContext.message();
    }

    @Override
    public MessageContext setLocaleResolver(LocaleResolver localeResolver)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public MessageContext setMessageInterpolator(MessageInterpolator messageInterpolator)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public MessageContext setMessageResolver(MessageResolver messageResolver)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale()
    {
        return messageContext.getLocale();
    }
}
