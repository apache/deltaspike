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
import java.util.Locale;

/**
 * {@inheritDoc}
 */
@Typed()
class DefaultMessageBuilder
{
    private String messageTemplate;
    private ArrayList<Object> argumentList;

    private final MessageContext messageContext;

    /**
     * Constructor for creating the builder which uses the given {@link MessageContext}
     * @param messageContext current message-context
     */
    public DefaultMessageBuilder(MessageContext messageContext)
    {
        reset();
        this.messageContext = messageContext;
    }


    private Message buildMessageTemplate()
    {
        if (messageTemplate == null)
        {
            throw new IllegalStateException("messageTemplate is missing");
        }

        return new DefaultMessage(messageContext, null, messageTemplate,
                argumentList.toArray(new Object[argumentList.size()]));
    }

    protected void reset()
    {
        messageTemplate = null;
        argumentList = new ArrayList<Object>();
    }

    public Message create()
    {
        Message result = buildMessageTemplate();
        reset();
        return result;
    }

    public String toText()
    {
        Message baseMessage = buildMessageTemplate();

        return getMessageText(baseMessage);
    }

    private String getMessageText(Message baseMessage)
    {
        String messageTemplate = baseMessage.getTemplate();

        MessageResolver messageResolver = messageContext.getMessageResolver();
        if (messageResolver != null)
        {
            messageTemplate = resolveMessage(messageResolver, baseMessage);
        }

        MessageInterpolator messageInterpolator = messageContext.getMessageInterpolator();

        //X TODO: this logic is odd. It's completely out of the standard flow
        //X TODO: handling is different based on where the interpolator gets configured :(
        if (messageInterpolator != null && messageTemplate != null)
        {
            return checkedResult(
                    interpolateMessage(messageInterpolator,
                            messageTemplate,
                            baseMessage.getArguments()),
                    baseMessage);
        }

        return checkedResult(messageTemplate, baseMessage);
    }

    private String checkedResult(String result, Message baseMessage)
    {
        if (result == null || isKey(baseMessage.getTemplate()) || isKeyWithoutMarkers(result, baseMessage))
        {
            //minor performance tweak for inline-msg
            String oldTemplate = extractTemplate(baseMessage.getTemplate());

            if (result == null || result.equals(oldTemplate) || result.equals(baseMessage.getTemplate()))
            {
                return MessageResolver.MISSING_RESOURCE_MARKER + oldTemplate +
                        MessageResolver.MISSING_RESOURCE_MARKER + getArguments(baseMessage);
            }
        }
        return result;
    }

    private boolean isKeyWithoutMarkers(String result, Message baseMessage)
    {
        return (!result.contains(" ") && result.endsWith(baseMessage.getTemplate()));
    }

    private String getArguments(Message message)
    {
        StringBuilder result = new StringBuilder();

        Object argument;
        Object[] arguments = message.getArguments();
        //TODO formatter

        if (arguments == null || arguments.length == 0)
        {
            return "";
        }

        for (int i = 0; i < arguments.length; i++)
        {
            if (i == 0)
            {
                result.append(" (");
            }
            else
            {
                result.append(",");
            }

            argument = arguments[i];

            result.append(argument.toString());
        }
        result.append(')');

        return result.toString();
    }


    private String extractTemplate(String template)
    {
        String result = getEscapedTemplate(template);

        if (isKey(result))
        {
            result = extractTemplateKey(result);
        }

        return result;
    }

    private boolean isKey(String key)
    {
        return key.startsWith("{") && key.endsWith("}");
    }

    private String extractTemplateKey(String key)
    {
        return key.substring(1, key.length() - 1);
    }

    private String resolveMessage(MessageResolver messageResolver, Message baseMessage)
    {
        return null; //X messageResolver.getMessage(baseMessage.getTemplate());
    }

    private String interpolateMessage(MessageInterpolator messageInterpolator,
                                      String messageTemplate, Object... arguments)
    {
        Locale l = messageContext.getLocale();
        return messageInterpolator.interpolate(getEscapedTemplate(messageTemplate), arguments, l);
    }

    private String getEscapedTemplate(String messageTemplate)
    {
        if (messageTemplate.startsWith("\\{"))
        {
            return messageTemplate.substring(1);
        }
        return messageTemplate;
    }
}
