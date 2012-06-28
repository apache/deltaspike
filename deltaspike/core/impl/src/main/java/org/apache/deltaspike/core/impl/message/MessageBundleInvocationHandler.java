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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.message.MessageContext;
import org.apache.deltaspike.core.api.message.MessageResolver;
import org.apache.deltaspike.core.api.message.annotation.MessageContextConfig;
import org.apache.deltaspike.core.api.message.annotation.MessageTemplate;
import org.apache.deltaspike.core.api.provider.BeanProvider;


class MessageBundleInvocationHandler implements InvocationHandler
{
    /**
     * Don't use this directly!
     * @see #getDefaultMessageContext()
     */
    private MessageContext defaultMessageContext = null;

    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     *      java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
    {
        final MessageTemplate messageTemplate = method.getAnnotation(MessageTemplate.class);

        if (messageTemplate == null)
        {
            // nothing to do... TODO discuss it
            return null;
        }


        MessageContext messageContext = resolveMessageContextFromArguments(args);
        List<Object> arguments = resolveMessageArguments(args);

        if (messageContext == null)
        {
            MessageContextConfig messageContextConfig =
                method.getDeclaringClass().getAnnotation(MessageContextConfig.class);

            if (messageContextConfig != null)
            {
                messageContext = applyMessageContextConfig(messageContextConfig, method.getDeclaringClass().getName());
            }
            else
            {
                messageContext = getDefaultMessageContext();
            }
        }


        if (String.class.isAssignableFrom(method.getReturnType()))
        {
            return messageContext.message().text(messageTemplate.value()).argument(arguments.toArray()).toText();
        }

        return messageContext.message().text(messageTemplate.value()).argument(arguments.toArray()).create();

    }

    private MessageContext applyMessageContextConfig(MessageContextConfig messageContextConfigLiteral,
                                                     String messageBundleName)
    {
        MessageContext.Config config = new DefaultMessageContextConfig(messageContextConfigLiteral);

        if (MessageResolver.class.equals(messageContextConfigLiteral.messageResolver()))
        {
            MessageResolver messageResolver = new DefaultMessageResolver();

            messageResolver.initialize(messageBundleName, config.getLocaleResolver().getLocale());
            config.change().messageResolver(messageResolver);
        }

        return config.use().create();
    }

    private List<Object> resolveMessageArguments(Object[] args)
    {
        List<Object> arguments = new ArrayList<Object>();
        if (args != null && args.length > 0)
        {
            for (int i = 0; i < args.length; i++)
            {
                if (i == 0 && MessageContext.class.isAssignableFrom(args[0].getClass()))
                {
                    continue;
                }

                arguments.add(args[i]);
            }
        }

        return arguments;
    }

    private MessageContext resolveMessageContextFromArguments(Object[] args)
    {
        if (args != null && args.length > 0 &&
            MessageContext.class.isAssignableFrom(args[0].getClass()))
        {
            return (MessageContext) args[0];
        }

        return null;
    }

    private MessageContext getDefaultMessageContext()
    {
        if (defaultMessageContext == null)
        {
            initDefaultConfig();
        }
        return defaultMessageContext;
    }


    /**
     * Lazily initialize {@link #defaultMessageContext}.
     */
    private synchronized void initDefaultConfig()
    {
        if (defaultMessageContext == null)
        {
            defaultMessageContext = BeanProvider.getContextualReference(MessageContext.class, new AnyLiteral());
        }
    }
}
