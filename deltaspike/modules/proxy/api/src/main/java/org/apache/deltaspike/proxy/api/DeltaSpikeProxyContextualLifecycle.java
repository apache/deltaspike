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
package org.apache.deltaspike.proxy.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.PassivationCapable;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

/**
 * {@link ContextualLifecycle} which handles a complete lifecycle of a proxy:
 * - creates a proxy via a {@link DeltaSpikeProxyFactory}
 * - handles the instantiation and injection of the proxy
 * - handles the instantiation via CDI of the delegate {@link InvocationHandler} and assign it to the proxy
 * - handles the release/destruction of both proxy and delegate {@link InvocationHandler}
 *
 * @param <T> The class of the original class.
 * @param <H> The class of the delegate {@link InvocationHandler}.
 */
public class DeltaSpikeProxyContextualLifecycle<T, H extends InvocationHandler> implements ContextualLifecycle<T>
{
    private final Class<T> proxyClass;
    private final Class<H> delegateInvocationHandlerClass;
    private final Class<T> targetClass;
    
    private InjectionTarget<T> injectionTarget;
    private CreationalContext<?> creationalContextOfDependentHandler;

    public DeltaSpikeProxyContextualLifecycle(Class<T> targetClass,
                                              Class<H> delegateInvocationHandlerClass,
                                              DeltaSpikeProxyFactory proxyFactory,
                                              BeanManager beanManager)
    {
        this.targetClass = targetClass;
        this.delegateInvocationHandlerClass = delegateInvocationHandlerClass;
        this.proxyClass = proxyFactory.getProxyClass(targetClass, delegateInvocationHandlerClass);

        if (!targetClass.isInterface())
        {
            AnnotatedType<T> annotatedType = beanManager.createAnnotatedType(this.targetClass);
            this.injectionTarget = beanManager.createInjectionTarget(annotatedType);
        }
    }

    @Override
    public T create(Bean bean, CreationalContext creationalContext)
    {
        try
        {
            T instance;

            if (delegateInvocationHandlerClass == null)
            {
                instance = proxyClass.newInstance();
            }
            else
            {
                H delegateInvocationHandler = instantiateDelegateInvocationHandler();
                Constructor<T> constructor = proxyClass.getConstructor(delegateInvocationHandlerClass);
                instance = constructor.newInstance(delegateInvocationHandler);
            }

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

        // can't happen
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
    
    protected H instantiateDelegateInvocationHandler()
    {
        Set<Bean<H>> handlerBeans = BeanProvider.getBeanDefinitions(this.delegateInvocationHandlerClass, false, true);
        
        if (handlerBeans.size() != 1)
        {
            StringBuilder beanInfo = new StringBuilder();
            for (Bean<H> bean : handlerBeans)
            {
                if (beanInfo.length() != 0)
                {
                    beanInfo.append(", ");
                }
                beanInfo.append(bean);

                if (bean instanceof PassivationCapable)
                {
                    beanInfo.append(" bean-id: ").append(((PassivationCapable)bean).getId());
                }
            }

            throw new IllegalStateException(handlerBeans.size() + " beans found for "
                    + this.delegateInvocationHandlerClass + " found beans: " + beanInfo.toString());
        }

        Bean<H> handlerBean = handlerBeans.iterator().next();
        
        BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();
        CreationalContext<?> creationalContext = beanManager.createCreationalContext(handlerBean);
        
        H handlerInstance = (H) beanManager.getReference(handlerBean,
                this.delegateInvocationHandlerClass, creationalContext);
        
        if (handlerBean.getScope().equals(Dependent.class))
        {
            this.creationalContextOfDependentHandler = creationalContext;
        }

        return handlerInstance;
    }
}
