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
import org.apache.deltaspike.core.api.message.Message;
import org.apache.deltaspike.core.api.message.MessageContextConfig;
import org.apache.deltaspike.core.api.message.MessageInterpolator;
import org.apache.deltaspike.core.api.message.MessageResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ClassUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
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
        final Message message = method.getAnnotation(Message.class);

        if (message == null)
        {
            // nothing to do...
            return null;
        }

        //X TODO discuss use-cases for a deeper lookup and qualifier support
        MessageContextConfig messageContextConfig =
            method.getDeclaringClass().getAnnotation(MessageContextConfig.class);

        if (messageContextConfig == null)
        {
            messageContextConfig = new MessageContextConfigLiteral();
        }

        String messageTemplate;

        if (!MessageResolver.class.equals(messageContextConfig.messageResolver()))
        {
            Class<? extends MessageResolver> messageResolverClass =
                    ClassUtils.tryToLoadClassForName(messageContextConfig.messageResolver().getName());

            MessageResolver messageResolver = BeanProvider.getContextualReference(messageResolverClass);

            messageTemplate = messageResolver.getMessage(message.value());
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
            messageTemplate = new DefaultMessageResolver(messageBundleName, resolvedLocale).getMessage(message.value());
        }

        Class<? extends MessageInterpolator> messageInterpolatorClass =
                ClassUtils.tryToLoadClassForName(messageContextConfig.messageInterpolator().getName());

        String result = messageTemplate;

        if (!MessageInterpolator.class.equals(messageInterpolatorClass))
        {
            MessageInterpolator messageInterpolator = BeanProvider.getContextualReference(messageInterpolatorClass);
            result = messageInterpolator.interpolate(messageTemplate, args);
        }

        return result;
    }
}
