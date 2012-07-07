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
import org.apache.deltaspike.core.api.message.LocaleResolver;
import org.apache.deltaspike.core.api.message.MessageContext;
import org.apache.deltaspike.core.api.message.MessageInterpolator;
import org.apache.deltaspike.core.api.message.MessageResolver;
import org.apache.deltaspike.core.api.message.annotation.MessageContextConfig;
import org.apache.deltaspike.core.api.message.annotation.MessageTemplate;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ClassUtils;


class MessageBundleInvocationHandler implements InvocationHandler
{

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
            messageContext = getDefaultMessageContext().clone();

            MessageContextConfig messageContextConfig =
                method.getDeclaringClass().getAnnotation(MessageContextConfig.class);

            if (messageContextConfig != null)
            {
                applyMessageContextConfig(messageContext, messageContextConfig);
            }
        }

        String messageBundleName = method.getDeclaringClass().getName();

        if (String.class.isAssignableFrom(method.getReturnType()))
        {
            return messageContext.message().bundle(messageBundleName).template(messageTemplate.value())
                    .argument(arguments.toArray()).toString();
        }

        return messageContext.message().bundle(messageBundleName).template(messageTemplate.value())
                .argument(arguments.toArray());

    }

    private void applyMessageContextConfig(MessageContext messageContext, MessageContextConfig messageContextConfig)
    {
        if (!MessageResolver.class.equals(messageContextConfig.messageResolver()))
        {
            Class<? extends MessageResolver> messageResolverClass =
                    ClassUtils.tryToLoadClassForName(messageContextConfig.messageResolver().getName());

            messageContext.messageResolver(
                    BeanProvider.getContextualReference(messageResolverClass, new AnyLiteral()));
        }

        if (!MessageInterpolator.class.equals(messageContextConfig.messageInterpolator()))
        {
            Class<? extends MessageInterpolator> messageInterpolatorClass =
                    ClassUtils.tryToLoadClassForName(messageContextConfig.messageInterpolator().getName());

            messageContext.messageInterpolator(
                    BeanProvider.getContextualReference(messageInterpolatorClass, new AnyLiteral()));
        }

        if (!LocaleResolver.class.equals(messageContextConfig.localeResolver()))
        {
            Class<? extends LocaleResolver> localeResolverClass =
                    ClassUtils.tryToLoadClassForName(messageContextConfig.localeResolver().getName());

            messageContext.localeResolver(
                    BeanProvider.getContextualReference(localeResolverClass, new AnyLiteral()));
        }
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
        return BeanProvider.getContextualReference(MessageContext.class);
    }

}
