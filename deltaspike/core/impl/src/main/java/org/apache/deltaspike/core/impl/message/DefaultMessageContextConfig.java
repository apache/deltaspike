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
 * {@inheritDoc}
 */
@Typed()
class DefaultMessageContextConfig implements MessageContext.Config
{
    private static final long serialVersionUID = 2919944628020782545L;

    private MessageInterpolator messageInterpolator;
    private MessageResolver messageResolver;
    private LocaleResolver localeResolver;

    DefaultMessageContextConfig()
    {
        resetMessageContextConfig();
    }

    private DefaultMessageContextConfig(MessageContext.Config messageContextConfigTemplate)
    {
        this();
        this.messageInterpolator = messageContextConfigTemplate.getMessageInterpolator();
        this.messageResolver = messageContextConfigTemplate.getMessageResolver();
        this.localeResolver = messageContextConfigTemplate.getLocaleResolver();
    }

    @Override
    public MessageContextBuilder use()
    {
        return new MessageContextBuilder()
        {
            private DefaultMessageContextConfig newMessageContextConfig =
                    new DefaultMessageContextConfig(DefaultMessageContextConfig.this);

            @Override
            public MessageContextBuilder messageInterpolator(MessageInterpolator messageInterpolator)
            {
                newMessageContextConfig.setMessageInterpolator(messageInterpolator);
                return this;
            }

            @Override
            public MessageContextBuilder messageResolver(MessageResolver messageResolver)
            {
                newMessageContextConfig.setMessageResolver(messageResolver);
                return this;
            }

            @Override
            public MessageContextBuilder localeResolver(LocaleResolver localeResolver)
            {
                newMessageContextConfig.setLocaleResolver(localeResolver);
                return this;
            }

            @Override
            public MessageContext create()
            {
                return new DefaultMessageContext(this.newMessageContextConfig);
            }
        };
    }

    @Override
    public MessageContextBuilder change()
    {
        return new MessageContextBuilder()
        {
            @Override
            public MessageContextBuilder messageInterpolator(MessageInterpolator messageInterpolator)
            {
                setMessageInterpolator(messageInterpolator);
                return this;
            }

            @Override
            public MessageContextBuilder messageResolver(MessageResolver messageResolver)
            {
                setMessageResolver(messageResolver);
                return this;
            }

            @Override
            public MessageContextBuilder localeResolver(LocaleResolver localeResolver)
            {
                setLocaleResolver(localeResolver);
                return this;
            }

            @Override
            public MessageContext create()
            {
                return new DefaultMessageContext(DefaultMessageContextConfig.this);
            }
        };
    }

    private void resetMessageContextConfig()
    {
        this.messageInterpolator = new DefaultMessageInterpolator();
        this.messageResolver = null;
        this.localeResolver = new DefaultLocaleResolver();
    }

    @Override
    public MessageInterpolator getMessageInterpolator()
    {
        return this.messageInterpolator;
    }

    @Override
    public MessageResolver getMessageResolver()
    {
        return this.messageResolver;
    }

    @Override
    public LocaleResolver getLocaleResolver()
    {
        return this.localeResolver;
    }

    private void setMessageInterpolator(MessageInterpolator messageInterpolator)
    {
        this.messageInterpolator = messageInterpolator;
    }

    private void setMessageResolver(MessageResolver messageResolver)
    {
        this.messageResolver = messageResolver;
    }

    private void setLocaleResolver(LocaleResolver localeResolver)
    {
        this.localeResolver = localeResolver;
    }

    @Override
    public String toString()
    {
        String newLine = System.getProperty("line.separator");

        StringBuilder configInfo = new StringBuilder("MessageContextConfig class: ");
        configInfo.append(getClass().getName());
        configInfo.append(newLine);

        if (this.messageInterpolator != null)
        {
            configInfo.append("   MessageInterpolator class: ").append(this.messageInterpolator.getClass());
        }
        else
        {
            configInfo.append("   no MessageInterpolator");
        }
        configInfo.append(newLine);

        if (this.messageResolver != null)
        {
            configInfo.append("   MessageResolver class: ").append(this.messageResolver.getClass());
        }
        else
        {
            configInfo.append("   no MessageResolver");
        }
        configInfo.append(newLine);

        configInfo.append(newLine);

        if (this.localeResolver != null)
        {
            configInfo.append("   LocaleResolver class: ").append(this.localeResolver.getClass());
        }
        else
        {
            configInfo.append("   no LocaleResolver");
        }
        configInfo.append(newLine);

        return configInfo.toString();
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
        if (!(o instanceof DefaultMessageContextConfig))
        {
            return false;
        }

        DefaultMessageContextConfig that = (DefaultMessageContextConfig) o;

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
