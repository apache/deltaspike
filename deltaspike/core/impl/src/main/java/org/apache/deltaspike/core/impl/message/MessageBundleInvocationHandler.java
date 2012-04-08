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

import org.apache.deltaspike.core.api.literal.MessageContextConfigLiteral;
import org.apache.deltaspike.core.api.message.LocaleResolver;
import org.apache.deltaspike.core.api.message.MessageContext;
import org.apache.deltaspike.core.api.message.MessageInterpolator;
import org.apache.deltaspike.core.api.message.MessageResolver;
import org.apache.deltaspike.core.api.message.annotation.MessageContextConfig;
import org.apache.deltaspike.core.api.message.annotation.MessageTemplate;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ClassUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        MessageContext messageContext = null;

        List<Object> arguments = null;

        if (args != null && args.length > 0)
        {
            arguments = new ArrayList<Object>(args.length);

            for (Object arg : args)
            {
                if (MessageContext.class.isAssignableFrom(arg.getClass()))
                {
                    messageContext = (MessageContext)arg;
                    continue;
                }

                arguments.add(arg);
            }
        }

        if (messageContext == null)
        {
            //X TODO discuss use-cases for a deeper lookup and qualifier support
            MessageContextConfig messageContextConfig =
                method.getDeclaringClass().getAnnotation(MessageContextConfig.class);

            if (messageContextConfig == null)
            {
                messageContextConfig = new MessageContextConfigLiteral();
            }

            String resolvedMessageTemplate;

            if (!MessageResolver.class.equals(messageContextConfig.messageResolver()))
            {
                Class<? extends MessageResolver> messageResolverClass =
                        ClassUtils.tryToLoadClassForName(messageContextConfig.messageResolver().getName());

                MessageResolver messageResolver = BeanProvider.getContextualReference(messageResolverClass);

                resolvedMessageTemplate = messageResolver.getMessage(messageTemplate.value());
            }
            else
            {
                Class<? extends LocaleResolver> localeResolverClass =
                        ClassUtils.tryToLoadClassForName(messageContextConfig.localeResolver().getName());

                Locale resolvedLocale = Locale.getDefault();

                if (!LocaleResolver.class.equals(localeResolverClass))
                {
                    LocaleResolver localeResolver = BeanProvider.getContextualReference(localeResolverClass);

                    resolvedLocale = localeResolver.getLocale();
                }

                String messageBundleName = method.getDeclaringClass().getName();
                resolvedMessageTemplate = new DefaultMessageResolver(messageBundleName, resolvedLocale)
                    .getMessage(messageTemplate.value());
            }

            Class<? extends MessageInterpolator> messageInterpolatorClass =
                    ClassUtils.tryToLoadClassForName(messageContextConfig.messageInterpolator().getName());

            String result = resolvedMessageTemplate;

            if (!MessageInterpolator.class.equals(messageInterpolatorClass))
            {
                MessageInterpolator messageInterpolator = BeanProvider.getContextualReference(messageInterpolatorClass);
                result = messageInterpolator.interpolate(resolvedMessageTemplate, args);
            }

            return result;
        }
        else
        {
            if (String.class.isAssignableFrom(method.getReturnType()))
            {
                return messageContext.message().text(messageTemplate.value()).argument(arguments.toArray()).toText();
            }

            return messageContext.message().text(messageTemplate.value()).argument(arguments.toArray()).create();
        }
    }
}
