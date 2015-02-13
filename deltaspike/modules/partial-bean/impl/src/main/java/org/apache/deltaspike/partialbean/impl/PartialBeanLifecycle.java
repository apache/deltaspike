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
package org.apache.deltaspike.partialbean.impl;

import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import java.lang.reflect.InvocationHandler;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.AnnotatedType;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.api.provider.BeanProvider;

class PartialBeanLifecycle<T, H extends InvocationHandler> implements ContextualLifecycle<T>
{
    private final Class<T> proxyClass;
    private final Class<T> partialBeanClass;
    private final Class<H> handlerClass;
    
    private InjectionTarget<T> injectionTarget;
    private CreationalContext<?> creationalContextOfDependentHandler;

    PartialBeanLifecycle(Class<T> partialBeanClass, Class<H> handlerClass, BeanManager beanManager)
    {
        this.partialBeanClass = partialBeanClass;
        this.proxyClass = PartialBeanProxyFactory.getProxyClass(partialBeanClass, handlerClass);
        this.handlerClass = handlerClass;

        if (!partialBeanClass.isInterface())
        {
            AnnotatedType<T> annotatedType = beanManager.createAnnotatedType(this.partialBeanClass);
            this.injectionTarget = beanManager.createInjectionTarget(annotatedType);
        }
    }

    @Override
    public T create(Bean bean, CreationalContext creationalContext)
    {
        try
        {
            T instance = proxyClass.newInstance();

            // only required here, for early partial beans, the handler will be injected and handled by CDI
            ((PartialBeanProxy) instance).setHandler(createHandlerInstance());

            if (this.injectionTarget != null)
            {
                this.injectionTarget.inject(instance, creationalContext);
                this.injectionTarget.postConstruct(instance);
            }

            return instance;
        }
        catch (Exception e)
        {
            ExceptionUtils.throwAsRuntimeException(e);
        }

        //can't happen
        return null;
    }

    @Override
    public void destroy(Bean<T> bean, T instance, CreationalContext<T> creationalContext)
    {
        if (this.injectionTarget != null)
        {
            this.injectionTarget.preDestroy(instance);
        }
        
        if (this.creationalContextOfDependentHandler != null)
        {
            this.creationalContextOfDependentHandler.release();
        }

        creationalContext.release();
    }
    
    private H createHandlerInstance()
    {
        Set<Bean<H>> handlerBeans = BeanProvider.getBeanDefinitions(this.handlerClass, false, true);
        
        if (handlerBeans.size() != 1)
        {
            throw new IllegalStateException(handlerBeans.size() + " beans found for " + this.handlerClass);
        }

        Bean<H> handlerBean = handlerBeans.iterator().next();
        
        BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();
        CreationalContext<?> creationalContext = beanManager.createCreationalContext(handlerBean);
        
        H handlerInstance = (H) beanManager.getReference(handlerBean, this.handlerClass, creationalContext);

        if (handlerBean.getScope().equals(Dependent.class))
        {
            this.creationalContextOfDependentHandler = creationalContext;
        }

        return handlerInstance;
    }
}

