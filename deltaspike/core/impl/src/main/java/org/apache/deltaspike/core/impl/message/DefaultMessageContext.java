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
import org.apache.deltaspike.core.api.message.Message;
import org.apache.deltaspike.core.api.message.MessageContext;
import org.apache.deltaspike.core.api.message.MessageInterpolator;
import org.apache.deltaspike.core.api.message.MessageResolver;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Dependent
@Typed(MessageContext.class)
class DefaultMessageContext implements MessageContext
{
    private static final long serialVersionUID = -110779217295211303L;


    @Inject
    private MessageInterpolator messageInterpolator = null;

    @Inject
    private MessageResolver messageResolver = null;

    @Inject
    private LocaleResolver localeResolver = null;

    private List<String> messageSources = new ArrayList<String>();

    DefaultMessageContext()
    {
    }

    DefaultMessageContext(MessageContext otherMessageContext)
    {
        messageInterpolator(otherMessageContext.getMessageInterpolator());
        localeResolver(otherMessageContext.getLocaleResolver());
        messageResolver(otherMessageContext.getMessageResolver());

        messageSources.addAll(otherMessageContext.getMessageSources());
    }

    @Override
    public MessageContext clone()
    {
        return new DefaultMessageContext(this);
    }

    @Override
    public Message message()
    {
        return new DefaultMessage(this);
    }

    @Override
    public MessageContext messageSource(String... messageSource)
    {
        List<String> newMessageSources = new ArrayList<String>();

        for (String currentMessageSource : messageSource)
        {
            //don't insert message-sources twice
            if (!messageSources.contains(currentMessageSource))
            {
                newMessageSources.add(currentMessageSource);
            }
        }
        // add on first position
        messageSources.addAll(0, newMessageSources);
        return this;
    }

    @Override
    public Locale getLocale()
    {
        if (getLocaleResolver() == null)
        {
            return Locale.getDefault();
        }

        return getLocaleResolver().getLocale();
    }

    @Override
    public LocaleResolver getLocaleResolver()
    {
        return localeResolver;
    }

    @Override
    public List<String> getMessageSources()
    {
        return Collections.unmodifiableList(messageSources);
    }

    @Override
    public MessageContext localeResolver(LocaleResolver localeResolver)
    {
        this.localeResolver = localeResolver;
        return this;
    }

    @Override
    public MessageInterpolator getMessageInterpolator()
    {
        return messageInterpolator;
    }

    @Override
    public MessageContext messageInterpolator(MessageInterpolator messageInterpolator)
    {
        this.messageInterpolator = messageInterpolator;
        return this;
    }

    @Override
    public MessageResolver getMessageResolver()
    {
        return messageResolver;
    }

    @Override
    public MessageContext messageResolver(MessageResolver messageResolver)
    {
        this.messageResolver = messageResolver;
        return this;
    }
}
