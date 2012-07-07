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

import org.apache.deltaspike.core.api.message.Message;
import org.apache.deltaspike.core.api.message.MessageContext;
import org.apache.deltaspike.core.api.message.MessageInterpolator;
import org.apache.deltaspike.core.api.message.MessageResolver;

import javax.enterprise.inject.Typed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.apache.deltaspike.core.api.message.MessageResolver.MISSING_RESOURCE_MARKER;

/**
 * {@inheritDoc}
 */
@Typed()
class DefaultMessage implements Message
{
    protected String messageTemplate;
    protected List<Object> arguments = new ArrayList<Object>();

    private MessageContext messageContext;

    DefaultMessage(MessageContext messageContext)
    {
        reset();

        this.messageContext = messageContext;
    }

    DefaultMessage(MessageContext messageContext,
                   String messageTemplate,
                   Object... arguments)
    {
        reset();

        this.messageContext = messageContext;
        this.messageTemplate = messageTemplate;

        Collections.addAll(this.arguments, arguments);
    }

    protected void reset()
    {
        messageTemplate = null;
        arguments = new ArrayList<Object>();
    }

    @Override
    public Message argument(Object... arguments)
    {
        Collections.addAll(this.arguments, arguments);
        return this;
    }

    @Override
    public Message template(String messageTemplate)
    {
        this.messageTemplate = messageTemplate;
        return this;
    }

    @Override
    public String getTemplate()
    {
        return messageTemplate;
    }

    @Override
    public Object[] getArguments()
    {
        return arguments.toArray();
    }


    @Override
    public String toString()
    {

        // the string construction happens in 3 phases

        // first try to pickup the message via the MessageResolver
        String template = getTemplate();
        String ret = template;
        MessageResolver messageResolver = messageContext.getMessageResolver();
        if (messageResolver != null)
        {
            String resolvedTemplate = messageResolver.getMessage(messageContext, template);
            if (resolvedTemplate == null)
            {
                // this means an error happened during message resolving
                resolvedTemplate = markAsUnresolved(template);
            }
            ret = resolvedTemplate;
            template = resolvedTemplate;
        }

        // last step is to interpolate the message
        MessageInterpolator messageInterpolator = messageContext.getMessageInterpolator();
        if (messageInterpolator != null)
        {
            Locale locale = messageContext.getLocale();

            ret = messageInterpolator.interpolate(template, getArguments(), locale);
        }

        return ret;
    }

    private String markAsUnresolved(String template)
    {
        if (messageTemplate.startsWith("{") && messageTemplate.endsWith("}"))
        {
            template = messageTemplate.substring(1, messageTemplate.length() - 1);
        }

        StringBuffer sb = new StringBuffer(MISSING_RESOURCE_MARKER + template + MISSING_RESOURCE_MARKER);
        if (getArguments() != null && getArguments().length > 0)
        {
            sb.append(" ").append(Arrays.toString(getArguments()));
        }

        return sb.toString();
    }

    public String toString(MessageContext messageContext)
    {
        return messageContext.message()
                .template(getTemplate())
                .argument(getArguments())
                .toString();
    }


    /**
     * Attention, the {@link #messageContext} is deliberately not part of the equation!
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Message))
        {
            return false;
        }

        Message that = (Message) o;

        if (!getTemplate().equals(that.getTemplate()))
        {
            return false;
        }

        //noinspection RedundantIfStatement
        if (arguments != null ? !Arrays.equals(arguments.toArray(), that.getArguments()) : that.getArguments() != null)
        {
            return false;
        }

        return true;
    }

    /**
     * Attention, the {@link #messageContext} is deliberately not part of the equation!
     */
    @Override
    public int hashCode()
    {
        int result = getTemplate().hashCode();
        result = 31 * result + (arguments != null ? arguments.hashCode() : 0);
        return result;
    }
}
