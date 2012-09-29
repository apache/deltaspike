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

package org.apache.deltaspike.core.util.context;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * A skeleton containing the most important parts of a custom CDI Contexts.
 */
public abstract class AbstractContext implements Context
{
    /**
     * We need the BeanManager for serialisation and some checks.
     */
    protected BeanManager beanManager;

    /**
     * The Scope the Context handles
     */
    protected Class<? extends Annotation> scope;


    protected AbstractContext(BeanManager beanManager, Class<? extends Annotation> scope, boolean concurrent)
    {
        this.beanManager = beanManager;
        this.scope = scope;
    }

    /**
     * An implementation has to return the underlying storage which
     * contains the items held in the Context.
     * @return the underlying storage
     */
    protected abstract ContextualStorage getContextStorage();


    @Override
    public Class<? extends Annotation> getScope()
    {
        return scope;
    }


    @Override
    public <T> T get(Contextual<T> component)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T> T get(Contextual<T> component, CreationalContext<T> creationalContext)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Destroy the Contextual Instance of the given Bean.
     * @param bean dictates which bean shall get cleaned up
     * @return <code>true</code> if the bean was destroyed, <code>false</code> if there was no such a bean.
     */
    public boolean destroy(Contextual bean)
    {
        ContextualInstanceInfo<?> contextualInstanceInfo = getContextStorage().getStorage().get(bean);

        if (contextualInstanceInfo == null)
        {
            return false;
        }

        bean.destroy(contextualInstanceInfo.getContextualInstance(), contextualInstanceInfo.getCreationalContext());

        return true;
    }

    /**
     * destroys all the Contextual Instances in the Context.
     */
    public void destroyAll()
    {
        Map<Contextual<?>, ContextualInstanceInfo<?>> storage = getContextStorage().getStorage();
        for (Map.Entry<Contextual<?>, ContextualInstanceInfo<?>> entry : storage.entrySet())
        {
            Contextual bean = entry.getKey();
            ContextualInstanceInfo<?> contextualInstanceInfo = entry.getValue();
            bean.destroy(contextualInstanceInfo.getContextualInstance(), contextualInstanceInfo.getCreationalContext());
        }


    }

}
