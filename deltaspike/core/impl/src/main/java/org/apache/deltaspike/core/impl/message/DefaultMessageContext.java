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

import javax.enterprise.inject.Typed;
import java.util.Locale;

@Typed()
class DefaultMessageContext implements MessageContext
{
    private static final long serialVersionUID = -110779217295211303L;

    private MessageInterpolator messageInterpolator = null;
    private MessageResolver messageResolver = null;
    private LocaleResolver localeResolver = null;

    DefaultMessageContext()
    {
    }

    DefaultMessageContext(MessageContext otherMessageContext)
    {
        messageInterpolator(otherMessageContext.getMessageInterpolator());
        localeResolver(otherMessageContext.getLocaleResolver());
        messageResolver(otherMessageContext.getMessageResolver()) ;
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
    public Locale getLocale()
    {
        if (getLocaleResolver() == null)
        {
            return Locale.getDefault();
        }

        return getLocaleResolver().getLocale();
    }

    public LocaleResolver getLocaleResolver()
    {
        return localeResolver;
    }

    public MessageContext localeResolver(LocaleResolver localeResolver)
    {
        this.localeResolver = localeResolver;
        return this;
    }

    public MessageInterpolator getMessageInterpolator()
    {
        return messageInterpolator;
    }

    public MessageContext messageInterpolator(MessageInterpolator messageInterpolator)
    {
        this.messageInterpolator = messageInterpolator;
        return this;
    }

    public MessageResolver getMessageResolver()
    {
        return messageResolver;
    }

    public MessageContext messageResolver(MessageResolver messageResolver)
    {
        this.messageResolver = messageResolver;
        return this;
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
        if (!(o instanceof DefaultMessageContext))
        {
            return false;
        }

        DefaultMessageContext that = (DefaultMessageContext) o;

        if (!localeResolver.equals(that.localeResolver))
        {
            return false;
        }
        if (messageInterpolator != null
                ? !messageInterpolator.equals(that.messageInterpolator) : that.messageInterpolator != null)
        {
            return false;
        }
        //noinspection RedundantIfStatement
        if (messageResolver != null ? !messageResolver.equals(that.messageResolver) : that.messageResolver != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = messageInterpolator != null ? messageInterpolator.hashCode() : 0;
        result = 31 * result + (messageResolver != null ? messageResolver.hashCode() : 0);
        result = 31 * result + localeResolver.hashCode();
        return result;
    }
}
