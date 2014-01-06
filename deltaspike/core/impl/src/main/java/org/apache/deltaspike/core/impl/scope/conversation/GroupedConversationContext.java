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
package org.apache.deltaspike.core.impl.scope.conversation;

import org.apache.deltaspike.core.api.scope.ConversationSubGroup;
import org.apache.deltaspike.core.api.scope.GroupedConversationScoped;
import org.apache.deltaspike.core.impl.scope.window.WindowContextImpl;
import org.apache.deltaspike.core.impl.util.ConversationUtils;
import org.apache.deltaspike.core.spi.scope.conversation.GroupedConversationManager;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualInstanceInfo;
import org.apache.deltaspike.core.util.context.ContextualStorage;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Typed()
//TODO add RequestCache
//TODO ConversationSubGroup
public class GroupedConversationContext extends AbstractContext implements GroupedConversationManager
{
    private static final long serialVersionUID = -5463564406828391468L;

    private final BeanManager beanManager;

    private final WindowContextImpl windowContext;

    private ConversationBeanHolder conversationBeanHolder;

    public GroupedConversationContext(BeanManager beanManager, WindowContextImpl windowContext)
    {
        super(beanManager);

        this.beanManager = beanManager;
        this.windowContext = windowContext;
    }

    public void init(ConversationBeanHolder conversationBeanHolder)
    {
        this.conversationBeanHolder = conversationBeanHolder;
    }

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist)
    {
        ConversationKey conversationKey = ConversationUtils.convertToConversationKey(contextual, this.beanManager);
        return this.conversationBeanHolder.getContextualStorage(this.beanManager, conversationKey, createIfNotExist);
    }

    @Override
    protected List<ContextualStorage> getActiveContextualStorages()
    {
        List<ContextualStorage> result = new ArrayList<ContextualStorage>();
        result.addAll(this.conversationBeanHolder.getStorageMap().values());
        return result;
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return GroupedConversationScoped.class;
    }

    @Override
    public boolean isActive()
    {
        return this.windowContext.isActive(); //autom. active once a window is active
    }

    @Override
    public ContextualStorage closeConversation(Class<?> conversationGroup, Annotation... qualifiers)
    {
        ConversationKey conversationKey = new ConversationKey(conversationGroup, qualifiers);
        ContextualStorage contextualStorage = this.conversationBeanHolder.getStorageMap().remove(conversationKey);

        if (contextualStorage != null)
        {
            AbstractContext.destroyAllActive(contextualStorage);
        }

        return contextualStorage;
    }

    @Override
    public Set<ContextualStorage> closeConversationGroup(Class<?> conversationGroup)
    {
        Set<ContextualStorage> result = new HashSet<ContextualStorage>();

        ConversationSubGroup conversationSubGroup = conversationGroup.getAnnotation(ConversationSubGroup.class);
        Set<Class<?>> subGroups = null;

        if (conversationSubGroup != null)
        {
            conversationGroup = ConversationUtils.getDeclaredConversationGroup(conversationGroup);

            subGroups = new HashSet<Class<?>>(conversationSubGroup.subGroup().length);
            Collections.addAll(subGroups, conversationSubGroup.subGroup());
        }

        Map<ConversationKey, ContextualStorage> storageMap = this.conversationBeanHolder.getStorageMap();
        for (Map.Entry<ConversationKey, ContextualStorage> entry : storageMap.entrySet())
        {
            if (entry.getKey().getConversationGroup().equals(conversationGroup))
            {
                if (subGroups == null)
                {
                    AbstractContext.destroyAllActive(entry.getValue());
                    result.add(entry.getValue());
                    storageMap.remove(entry.getKey()); //ok due to ConcurrentHashMap
                }
                else
                {
                    tryToDestroySubGroup(subGroups, entry);

                    if (entry.getValue().getStorage().isEmpty())
                    {
                        storageMap.remove(entry.getKey()); //ok due to ConcurrentHashMap
                    }
                }
            }
        }
        return result;
    }

    private void tryToDestroySubGroup(Set<Class<?>> subGroups, Map.Entry<ConversationKey, ContextualStorage> entry)
    {
        ContextualStorage storage = entry.getValue();

        for (Map.Entry<Object, ContextualInstanceInfo<?>> storageEntry : storage.getStorage().entrySet())
        {
            for (Class<?> subGroup : subGroups)
            {
                Class classOfEntry = storageEntry.getValue().getContextualInstance().getClass();
                if (subGroup.equals(classOfEntry) ||
                    (subGroup.isInterface() && subGroup.isAssignableFrom(classOfEntry)))
                {
                    Contextual bean = storage.getBean(storageEntry.getKey());
                    AbstractContext.destroyBean(bean, storageEntry.getValue());
                    storage.getStorage().remove(storageEntry.getKey()); //ok due to ConcurrentHashMap
                    break;
                }
            }
        }
    }

    @Override
    public void closeConversations()
    {
        this.conversationBeanHolder.destroyBeans();
    }
}
