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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.PassivationCapable;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.apache.deltaspike.proxy.spi.DeltaSpikeProxy;
import org.apache.deltaspike.proxy.spi.invocation.DeltaSpikeProxyInvocationHandler;

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
    private final Method[] delegateMethods;
    private final Class<T> targetClass;
    private final BeanManager beanManager;
    
    private volatile DeltaSpikeProxyInvocationHandler deltaSpikeProxyInvocationHandler;
    
    private volatile InjectionTarget<T> injectionTarget;
    private volatile Bean<H> handlerBean;
    private volatile CreationalContext<?> creationalContextOfDependentHandler;

    public DeltaSpikeProxyContextualLifecycle(Class<T> targetClass,
                                              Class<H> delegateInvocationHandlerClass,
                                              DeltaSpikeProxyFactory proxyFactory,
                                              BeanManager beanManager)
    {
        this.targetClass = targetClass;
        this.delegateInvocationHandlerClass = delegateInvocationHandlerClass;
        this.proxyClass = proxyFactory.getProxyClass(beanManager, targetClass);
        this.delegateMethods = proxyFactory.getDelegateMethods(targetClass);
        this.beanManager = beanManager;
        
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
            lazyInit();
            
            T instance = proxyClass.newInstance();

            DeltaSpikeProxy deltaSpikeProxy = ((DeltaSpikeProxy) instance);
            deltaSpikeProxy.setInvocationHandler(deltaSpikeProxyInvocationHandler);

            // optional 
            if (delegateInvocationHandlerClass != null)
            {
                H delegateInvocationHandler = instantiateDelegateInvocationHandler();
                deltaSpikeProxy.setDelegateInvocationHandler(delegateInvocationHandler);
                deltaSpikeProxy.setDelegateMethods(delegateMethods);
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
    
    private void lazyInit()
    {
        if (this.deltaSpikeProxyInvocationHandler == null)
        {
            init();
        }
    }

    private synchronized void init()
    {
        if (this.deltaSpikeProxyInvocationHandler == null)
        {
            Set<Bean<H>> handlerBeans = BeanProvider.getBeanDefinitions(
                    delegateInvocationHandlerClass, false, true, beanManager);
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
                        beanInfo.append(" bean-id: ").append(((PassivationCapable) bean).getId());
                    }
                }

                throw new IllegalStateException(handlerBeans.size() + " beans found for "
                        + delegateInvocationHandlerClass + " found beans: " + beanInfo.toString());
            }
            this.handlerBean = handlerBeans.iterator().next();

            this.deltaSpikeProxyInvocationHandler = BeanProvider.getContextualReference(
                    beanManager, DeltaSpikeProxyInvocationHandler.class, false);
        }
    }
    
    protected H instantiateDelegateInvocationHandler()
    {
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
