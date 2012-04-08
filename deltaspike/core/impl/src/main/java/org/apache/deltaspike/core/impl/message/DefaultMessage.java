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

import javax.enterprise.inject.Typed;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@inheritDoc}
 */
@Typed()
class DefaultMessage implements Message
{
    protected String messageTemplate;
    protected List<Object> arguments = new ArrayList<Object>();

    private MessageContext.Config messageContextConfig;

    DefaultMessage(MessageContext.Config messageContextConfig,
                   String messageTemplate,
                   Object... arguments)
    {
        this.messageContextConfig = messageContextConfig;
        this.messageTemplate = messageTemplate;
        this.arguments.addAll(Arrays.asList(arguments));
    }

    @Override
    public Message addArgument(Object... arguments)
    {
        Object argument;
        for (Object currentArgument : arguments)
        {
            argument = currentArgument;

            if (isHiddenArgument(argument))
            {
                addHiddenArgument(argument);
            }
            else
            {
                addArgumentToMessage(argument);
            }
        }
        return this;
    }

    private void addArgumentToMessage(Object argument)
    {
        //TODO discuss Localizable
        String result;

        if (argument instanceof String)
        {
            result = (String) argument;
        }
        else if (argument == null)
        {
            result = "null";
        }
        else
        {
            result = argument.toString();
        }

        addNumberedArgument(result);
    }

    private boolean isHiddenArgument(Object argument)
    {
        return argument != null && argument.getClass().isArray();
    }

    private void addHiddenArgument(Object argument)
    {
        for (Object current : ((Object[]) argument))
        {
            addArgumentToMessage(current);
        }
    }

    protected void addNumberedArgument(Serializable argument)
    {
        if (this.arguments == null)
        {
            this.arguments = new ArrayList<Object>();
        }

        this.arguments.add(argument);
    }

    @Override
    public String getMessageTemplate()
    {
        return this.messageTemplate;
    }

    @Override
    public Object[] getArguments()
    {
        return this.arguments.toArray();
    }


    @Override
    public String toString()
    {
        return toString(new DefaultMessageContext(this.messageContextConfig));
    }

    public String toString(MessageContext messageContext)
    {
        return messageContext.message()
                .text(getMessageTemplate())
                .argument(getArguments())
                .toText();
    }

    /*
     * generated
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

        if (!getMessageTemplate().equals(that.getMessageTemplate()))
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

    @Override
    public int hashCode()
    {
        int result = getMessageTemplate().hashCode();
        result = 31 * result + (arguments != null ? arguments.hashCode() : 0);
        return result;
    }
}
