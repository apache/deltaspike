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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.apache.deltaspike.core.api.message.MessageResolver.MISSING_RESOURCE_MARKER;

/**
 * {@inheritDoc}
 */
@Typed()
public class DefaultMessage implements Message
{
    private String messageTemplate;
    private List<Serializable> arguments = new ArrayList<Serializable>();

    private MessageContext messageContext;

    public DefaultMessage(MessageContext messageContext)
    {
        reset();

        this.messageContext = messageContext;
    }

    protected void reset()
    {
        messageTemplate = null;
        arguments = new ArrayList<Serializable>();
    }

    @Override
    public Message argument(Serializable... arguments)
    {
        if (arguments != null)
        {
            Collections.addAll(this.arguments, arguments);
        }
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
    public Serializable[] getArguments()
    {
        return arguments.toArray(new Serializable[arguments.size()]);
    }



    @Override
    public String toString()
    {
        return toString((String) null);
    }

    @Override
    public String toString(String category)
    {

        // the string construction happens in 3 phases

        // first try to pickup the message via the MessageResolver
        String template = getTemplate();
        if (template == null)
        {
            return "";
        }

        String ret = template;
        MessageResolver messageResolver = messageContext.getMessageResolver();
        if (messageResolver != null)
        {
            String resolvedTemplate = messageResolver.getMessage(messageContext, template, category);

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

        StringBuilder sb = new StringBuilder(MISSING_RESOURCE_MARKER + template + MISSING_RESOURCE_MARKER);
        if (getArguments() != null && getArguments().length > 0)
        {
            sb.append(" ").append(Arrays.toString(getArguments()));
        }

        return sb.toString();
    }

    @Override
    public String toString(MessageContext messageContext)
    {
        return toString(messageContext, null);
    }

    @Override
    public String toString(MessageContext messageContext, String category)
    {
        return messageContext.message()
                .template(getTemplate())
                .argument(getArguments())
                .toString(category);
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

        Message other = (Message) o;

        if (getTemplate() == null && other.getTemplate() != null)
        {
            return false;
        }

        if (getTemplate() != null && !getTemplate().equals(other.getTemplate()))
        {
            return false;
        }

        //noinspection RedundantIfStatement
        if (arguments != null
                ? !Arrays.equals(arguments.toArray(), other.getArguments())
                : other.getArguments() != null)
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

    @Override
    public Message argumentArray(Serializable[] arguments)
    {
        if (arguments != null)
        {
            return argument(Arrays.asList(arguments)); 
        }
        return this;
    }

    @Override
    public Message argument(Collection<Serializable> arguments)
    {
        if (arguments != null)
        {
            this.arguments.addAll(arguments); 
        }
        return this;
    }
}
