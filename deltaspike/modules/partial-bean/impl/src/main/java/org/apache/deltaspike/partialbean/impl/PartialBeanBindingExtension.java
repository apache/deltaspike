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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.apache.deltaspike.partialbean.api.PartialBeanBinding;
import org.apache.deltaspike.proxy.api.DeltaSpikeProxyContextualLifecycle;

public class PartialBeanBindingExtension implements Extension, Deactivatable
{
    private final Map<Class<? extends Annotation>, PartialBeanDescriptor> descriptors =
            new HashMap<Class<? extends Annotation>, PartialBeanDescriptor>();

    private Boolean isActivated = true;
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

        // skip classes without a partial bean binding
        Class<? extends Annotation> bindingClass = extractBindingClass(pat);
        if (bindingClass == null)
        {
            return;
        }

        if (beanClass.isInterface() || Modifier.isAbstract(beanClass.getModifiers()))
        {
            pat.veto();

            PartialBeanDescriptor descriptor = descriptors.get(bindingClass);

            if (descriptor == null)
            {
                descriptor = new PartialBeanDescriptor(bindingClass, null, beanClass);
                descriptors.put(bindingClass, descriptor);
            }
            else if (!descriptor.getClasses().contains(beanClass))
            {
                descriptor.getClasses().add(beanClass);
            }
        }
        else if (InvocationHandler.class.isAssignableFrom(beanClass))
        {
            PartialBeanDescriptor descriptor = descriptors.get(bindingClass);

            if (descriptor == null)
            {
                descriptor = new PartialBeanDescriptor(bindingClass, (Class<? extends InvocationHandler>) beanClass);
                descriptors.put(bindingClass, descriptor);
            }
            else
            {
                if (descriptor.getHandler() == null)
                {
                    descriptor.setHandler((Class<? extends InvocationHandler>) beanClass);
                }
                else if (!descriptor.getHandler().equals(beanClass))
                {
                    this.definitionError = new IllegalStateException("Multiple handlers found for "
                            + bindingClass.getName() + " ("
                            + descriptor.getHandler().getName()
                            + " and " + beanClass.getName() + ")");
                }
            }
        }
        else
        {
            this.definitionError = new IllegalStateException(beanClass.getName() + " is annotated with @"
                    + bindingClass.getName() + " and therefore has to be "
                    + "an abstract class, an interface or an implementation of " + InvocationHandler.class.getName());
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

        for (Map.Entry<Class<? extends Annotation>, PartialBeanDescriptor> entry : this.descriptors.entrySet())
        {
            PartialBeanDescriptor descriptor = entry.getValue();
            if (descriptor.getClasses() != null)
            {
                for (Class partialBeanClass : descriptor.getClasses())
                {
                    Bean partialBean = createPartialBean(partialBeanClass, descriptor, afterBeanDiscovery, beanManager);
                    if (partialBean != null)
                    {
                        afterBeanDiscovery.addBean(partialBean);
                    }
                }
            }
        }

        this.descriptors.clear();
    }


    protected <T> Bean<T> createPartialBean(Class<T> beanClass, PartialBeanDescriptor descriptor,
            AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        if (descriptor.getHandler() == null)
        {
            afterBeanDiscovery.addDefinitionError(new IllegalStateException("A class which implements "
                    + InvocationHandler.class.getName()
                    + " and is annotated with @" + descriptor.getBinding().getName()
                    + " is needed as a handler for " + beanClass.getName()
                    + ". See the documentation about @" + PartialBeanBinding.class.getName() + "."));

            return null;
        }

        AnnotatedType<T> annotatedType = new AnnotatedTypeBuilder<T>().readFromType(beanClass).create();

        DeltaSpikeProxyContextualLifecycle lifecycle = new DeltaSpikeProxyContextualLifecycle(beanClass,
                descriptor.getHandler(),
                PartialBeanProxyFactory.getInstance(),
                beanManager);

        BeanBuilder<T> beanBuilder = new BeanBuilder<T>(beanManager)
                .readFromType(annotatedType)
                .passivationCapable(true)
                .beanLifecycle(lifecycle);

        return beanBuilder.create();
    }

    protected <X> Class<? extends Annotation> extractBindingClass(ProcessAnnotatedType<X> pat)
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
}
