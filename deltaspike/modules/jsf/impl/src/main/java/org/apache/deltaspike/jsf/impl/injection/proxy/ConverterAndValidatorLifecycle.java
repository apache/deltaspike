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
package org.apache.deltaspike.jsf.impl.injection.proxy;

import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.faces.component.PartialStateHolder;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ConverterAndValidatorLifecycle<T, H extends InvocationHandler> implements ContextualLifecycle<T>
{
    private final Class<? extends T> generatedProxyClass;

    private final InjectionTarget<T> injectionTargetForGeneratedProxy;
    private final Class<H> handlerClass;

    ConverterAndValidatorLifecycle(Class<T> originalClass, Class<H> handlerClass, BeanManager beanManager)
    {
        this.handlerClass = handlerClass;

        AnnotatedTypeBuilder<T> typeBuilder = new AnnotatedTypeBuilder<T>().readFromType(originalClass);
        this.injectionTargetForGeneratedProxy = beanManager.createInjectionTarget(typeBuilder.create());

        try
        {
            Object proxyFactory = ClassUtils.tryToInstantiateClassForName("javassist.util.proxy.ProxyFactory");

            Method setSuperclassMethod = proxyFactory.getClass().getDeclaredMethod("setSuperclass", Class.class);
            setSuperclassMethod.invoke(proxyFactory, originalClass);

            List<Class> interfaces = new ArrayList<Class>();
            Collections.addAll(interfaces, originalClass.getInterfaces());
            interfaces.add(ProxyMarker.class);

            if (!interfaces.contains(PartialStateHolder.class))
            {
                interfaces.add(PartialStateHolder.class);
            }

            Method method = proxyFactory.getClass().getMethod("setInterfaces", new Class[]{new Class[]{}.getClass()});
            method.invoke(proxyFactory, new Object[] {interfaces.toArray(new Class[interfaces.size()])});

            Method createClassMethod = proxyFactory.getClass().getDeclaredMethod("createClass");

            this.generatedProxyClass = ((Class<?>) createClassMethod.invoke(proxyFactory)).asSubclass(originalClass);
        }
        catch (Exception e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    public T create(Bean bean, CreationalContext creationalContext)
    {
        try
        {
            H handlerInstance = ClassUtils.tryToInstantiateClass(this.handlerClass);
            T instance = createProxyInstance(handlerInstance);

            if (this.injectionTargetForGeneratedProxy != null)
            {
                this.injectionTargetForGeneratedProxy.inject(instance, creationalContext);
                this.injectionTargetForGeneratedProxy.postConstruct(instance);
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

    private T createProxyInstance(H handlerInstance) throws Exception
    {
        T instance = this.generatedProxyClass.newInstance();

        Class methodHandlerClass = ClassUtils.tryToLoadClassForName("javassist.util.proxy.MethodHandler");
        Method setHandlerMethod = ClassUtils.tryToLoadClassForName("javassist.util.proxy.ProxyObject")
            .getDeclaredMethod("setHandler", methodHandlerClass);


        MethodHandlerProxy methodHandlerProxy = new MethodHandlerProxy();
        methodHandlerProxy.setDelegatingMethodHandler(new DelegatingMethodHandler<H>(handlerInstance));

        Object methodHandler = Proxy.newProxyInstance(
                ClassUtils.getClassLoader(this), new Class[]{methodHandlerClass}, methodHandlerProxy);

        setHandlerMethod.invoke(instance, methodHandler);
        return instance;
    }

    public void destroy(Bean<T> bean, T instance, CreationalContext<T> creationalContext)
    {
        if (this.injectionTargetForGeneratedProxy != null)
        {
            this.injectionTargetForGeneratedProxy.preDestroy(instance);
        }

        creationalContext.release();
    }
}
