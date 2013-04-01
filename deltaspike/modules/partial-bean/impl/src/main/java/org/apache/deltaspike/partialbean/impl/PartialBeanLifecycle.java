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

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

//The usage of reflection and the indirection for MethodHandler is needed to avoid the hard dependency to javassist.
//Some users don't like to have it as a required dependency,
// but they would like to use interfaces for partial beans and don't need abstract classes as partial beans.

//We use these indirections as >intermediate< approach. That way no classpath-scanner can cause issues.
class PartialBeanLifecycle<T, H extends InvocationHandler> implements ContextualLifecycle<T>
{
    private final Class<? extends T> partialBeanProxyClass;

    private final InjectionTarget<T> partialBeanInjectionTarget;
    private final Class<H> handlerClass;
    private CreationalContext<?> creationalContextOfDependentHandler;
    private final boolean isInterfaceMode;

    PartialBeanLifecycle(Class<T> partialBeanClass, Class<H> handlerClass, BeanManager beanManager)
    {
        this.handlerClass = handlerClass;

        if (partialBeanClass.isInterface())
        {
            this.isInterfaceMode = true;
            this.partialBeanInjectionTarget = null;
            this.partialBeanProxyClass = partialBeanClass;
        }
        else
        {
            this.isInterfaceMode = false;
            AnnotatedTypeBuilder<T> partialBeanTypeBuilder =
                new AnnotatedTypeBuilder<T>().readFromType(partialBeanClass);
            this.partialBeanInjectionTarget = beanManager.createInjectionTarget(partialBeanTypeBuilder.create());

            try
            {
                Object proxyFactory = ClassUtils.tryToInstantiateClassForName("javassist.util.proxy.ProxyFactory");

                Method setSuperclassMethod = proxyFactory.getClass().getDeclaredMethod("setSuperclass", Class.class);
                setSuperclassMethod.invoke(proxyFactory, partialBeanClass);

                Method createClassMethod = proxyFactory.getClass().getDeclaredMethod("createClass");

                this.partialBeanProxyClass =
                        ((Class<?>) createClassMethod.invoke(proxyFactory)).asSubclass(partialBeanClass);
            }
            catch (Exception e)
            {
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }

        /*TODO re-visit the need of MethodFilter - we would need an indirection for it as with MethodHandler
        proxyFactory.setFilter(new MethodFilter()
        {
            public boolean isHandled(Method method)
            {
                return !"finalize".equals(method.getName());
            }
        });
         */
    }

    public T create(Bean bean, CreationalContext creationalContext)
    {
        try
        {
            H handlerInstance = createHandlerInstance();
            T instance = createPartialBeanProxyInstance(handlerInstance);

            if (this.partialBeanInjectionTarget != null)
            {
                this.partialBeanInjectionTarget.inject(instance, creationalContext);
                this.partialBeanInjectionTarget.postConstruct(instance);
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

    private T createPartialBeanProxyInstance(H handlerInstance) throws Exception
    {
        T instance;

        if (this.isInterfaceMode)
        {
            instance = (T) Proxy.newProxyInstance(
                    ClassUtils.getClassLoader(this), new Class[]{this.partialBeanProxyClass}, handlerInstance);
        }
        else //partial-bean is an interface
        {
            instance = this.partialBeanProxyClass.newInstance();

            Class methodHandlerClass = ClassUtils.tryToLoadClassForName("javassist.util.proxy.MethodHandler");
            Method setHandlerMethod = ClassUtils.tryToLoadClassForName("javassist.util.proxy.ProxyObject")
                    .getDeclaredMethod("setHandler", methodHandlerClass);


            MethodHandlerProxy methodHandlerProxy = new MethodHandlerProxy();
            methodHandlerProxy.setPartialBeanMethodHandler(new PartialBeanAbstractMethodHandler<H>(handlerInstance));

            Object methodHandler = Proxy.newProxyInstance(
                    ClassUtils.getClassLoader(this), new Class[]{methodHandlerClass}, methodHandlerProxy);

            setHandlerMethod.invoke(instance, methodHandler);
        }
        return instance;
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
        CreationalContext<?> creationalContextOfHandler = beanManager.createCreationalContext(handlerBean);

        H handlerInstance = (H)beanManager.getReference(handlerBean, this.handlerClass, creationalContextOfHandler);

        if (handlerBean.getScope().equals(Dependent.class))
        {
            this.creationalContextOfDependentHandler = creationalContextOfHandler;
        }
        return handlerInstance;
    }

    public void destroy(Bean<T> bean, T instance, CreationalContext<T> creationalContext)
    {
        if (this.partialBeanInjectionTarget != null)
        {
            this.partialBeanInjectionTarget.preDestroy(instance);
        }

        if (this.creationalContextOfDependentHandler != null)
        {
            this.creationalContextOfDependentHandler.release();
        }

        /*
        H handlerInstance = (H) ((PartialBeanAbstractMethodHandler)((ProxyObject) instance)
          .getHandler()).getHandlerInstance();
        injectionTarget.dispose(handlerInstance); //currently producers aren't supported
        }
        */
        creationalContext.release();
    }
}
