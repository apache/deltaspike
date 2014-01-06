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
package org.apache.deltaspike.core.impl.util;

import org.apache.deltaspike.core.api.scope.ConversationGroup;
import org.apache.deltaspike.core.api.scope.ConversationSubGroup;
import org.apache.deltaspike.core.impl.scope.conversation.ConversationKey;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.PassivationCapable;
import java.lang.annotation.Annotation;
import java.util.Set;

@Typed()
public abstract class ConversationUtils
{
    private ConversationUtils()
    {
    }

    public static ConversationKey convertToConversationKey(Contextual<?> contextual, BeanManager beanManager)
    {
        if (!(contextual instanceof Bean))
        {
            if (contextual instanceof PassivationCapable)
            {
                contextual = beanManager.getPassivationCapableBean(((PassivationCapable) contextual).getId());
            }
            else
            {
                throw new IllegalArgumentException(
                    contextual.getClass().getName() + " is not of type " + Bean.class.getName());
            }
        }

        Bean<?> bean = (Bean<?>) contextual;

        //don't cache it (due to the support of different producers)
        ConversationGroup conversationGroupAnnotation = findConversationGroupAnnotation(bean);

        Class<?> conversationGroup;
        if (conversationGroupAnnotation != null)
        {
            conversationGroup = conversationGroupAnnotation.value();
        }
        else
        {
            conversationGroup = bean.getBeanClass();
        }

        Set<Annotation> qualifiers = bean.getQualifiers();
        return new ConversationKey(conversationGroup, qualifiers.toArray(new Annotation[qualifiers.size()]));
    }

    private static ConversationGroup findConversationGroupAnnotation(Bean<?> bean)
    {
        Set<Annotation> qualifiers = bean.getQualifiers();

        for (Annotation qualifier : qualifiers)
        {
            if (ConversationGroup.class.isAssignableFrom(qualifier.annotationType()))
            {
                return (ConversationGroup) qualifier;
            }
        }
        return null;
    }

    public static Class<?> getDeclaredConversationGroup(Class<?> conversationGroup)
    {
        ConversationSubGroup conversationSubGroup = conversationGroup.getAnnotation(ConversationSubGroup.class);

        if (conversationSubGroup == null)
        {
            return conversationGroup;
        }

        Class<?> result = conversationSubGroup.of();

        if (!ConversationSubGroup.class.equals(result))
        {
            return result;
        }

        result = conversationGroup.getSuperclass();

        if ((result == null || Object.class.getName().equals(result.getName())) &&
                conversationGroup.getInterfaces().length == 1)
        {
            return conversationGroup.getInterfaces()[0];
        }

        if (result == null)
        {
            //TODO move validation to the bootstrapping process
            throw new IllegalStateException(conversationGroup.getName() + " hosts an invalid usage of @" +
                ConversationSubGroup.class.getName());
        }
        return result;
    }
}
