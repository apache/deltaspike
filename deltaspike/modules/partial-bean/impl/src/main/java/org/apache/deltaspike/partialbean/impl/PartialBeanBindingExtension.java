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

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.apache.deltaspike.partialbean.api.PartialBeanBinding;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class PartialBeanBindingExtension implements Extension, Deactivatable
{
    private static final Logger LOG = Logger.getLogger(PartialBeanBindingExtension.class.getName());

    private Boolean isActivated = true;
    private Map<Class<?>, Class<? extends Annotation>> partialBeans =
            new HashMap<Class<?>, Class<? extends Annotation>>();
    private Map<Class<? extends Annotation>, Class<? extends InvocationHandler>> partialBeanHandlers =
            new HashMap<Class<? extends Annotation>, Class<? extends InvocationHandler>>();

    private IllegalStateException definitionError;

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        this.isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    public <X> void findInvocationHandlerBindings(@Observes ProcessAnnotatedType<X> pat, BeanManager beanManager)
    {
        if (!this.isActivated || this.definitionError != null)
        {
            return;
        }

        Class<X> beanClass = pat.getAnnotatedType().getJavaClass();
        Class<? extends Annotation> bindingAnnotationClass = getInvocationHandlerBindingAnnotationClass(pat);

        if (bindingAnnotationClass == null)
        {
            return;
        }

        if ((beanClass.isInterface() || Modifier.isAbstract(beanClass.getModifiers())))
        {
            this.partialBeans.put(beanClass, bindingAnnotationClass);
        }
        else if (InvocationHandler.class.isAssignableFrom(beanClass))
        {
            validateInvocationHandler(
                    beanClass, bindingAnnotationClass, pat.getAnnotatedType().getAnnotations(), beanManager);

            this.partialBeanHandlers.put(bindingAnnotationClass, (Class<? extends InvocationHandler>) beanClass);
        }
        else
        {
            this.definitionError = new IllegalStateException(beanClass.getName() + " is annotated with @" +
                bindingAnnotationClass.getName() + " and therefore has to be " +
                "an abstract class, an interface or an implementation of " + InvocationHandler.class.getName());
        }
    }

    public <X> void createBeans(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        if (!this.isActivated)
        {
            return;
        }

        if (this.definitionError != null)
        {
            afterBeanDiscovery.addDefinitionError(this.definitionError);
            return;
        }

        for (Map.Entry<Class<?>, Class<? extends Annotation>> partialBeanEntry : this.partialBeans.entrySet())
        {
            Bean partialBean = createPartialBean(partialBeanEntry.getKey(), partialBeanEntry.getValue(), beanManager);

            if (partialBean != null)
            {
                afterBeanDiscovery.addBean(partialBean);
            }
            else
            {
                afterBeanDiscovery.addDefinitionError(new IllegalStateException("A class which implements " +
                        InvocationHandler.class.getName() + " and is annotated with @" +
                        partialBeanEntry.getValue().getName() + " is needed as a handler for " +
                        partialBeanEntry.getKey().getName() + ". See the documentation about @" +
                        PartialBeanBinding.class.getName() + "."));
            }
        }

        this.partialBeans.clear();
        this.partialBeanHandlers.clear();
    }

    protected <T> Bean<T> createPartialBean(Class<T> beanClass,
                                            Class<? extends Annotation> bindingAnnotationClass,
                                            BeanManager beanManager)
    {
        Class<? extends InvocationHandler> invocationHandlerClass = partialBeanHandlers.get(bindingAnnotationClass);

        if (invocationHandlerClass == null)
        {
            return null;
        }

        AnnotatedType<T> annotatedType = new AnnotatedTypeBuilder<T>().readFromType(beanClass).create();

        BeanBuilder<T> beanBuilder = new BeanBuilder<T>(beanManager)
                .readFromType(annotatedType)
                .passivationCapable(true)
                .beanLifecycle(new PartialBeanLifecycle(beanClass, invocationHandlerClass, beanManager));

        return beanBuilder.create();
    }

    protected <X> Class<? extends Annotation> getInvocationHandlerBindingAnnotationClass(ProcessAnnotatedType<X> pat)
    {
        for (Annotation annotation : pat.getAnnotatedType().getAnnotations())
        {
            if (annotation.annotationType().isAnnotationPresent(PartialBeanBinding.class))
            {
                return annotation.annotationType();
            }
        }

        return null;
    }

    protected <X> void validateInvocationHandler(Class<X> beanClass,
                                                 Class<? extends Annotation> bindingAnnotationClass,
                                                 Set<Annotation> annotations,
                                                 BeanManager beanManager)
    {
        Class<? extends InvocationHandler> alreadyFoundHandler = this.partialBeanHandlers.get(bindingAnnotationClass);
        if (alreadyFoundHandler != null)
        {
            this.definitionError = new IllegalStateException("Multiple handlers found for " +
                    bindingAnnotationClass.getName() + " (" +
                    alreadyFoundHandler.getName() + " and " + beanClass.getName() + ")");
        }
    }
}
