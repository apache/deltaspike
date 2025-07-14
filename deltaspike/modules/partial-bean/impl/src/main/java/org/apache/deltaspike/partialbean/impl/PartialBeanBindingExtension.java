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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.configurator.BeanConfigurator;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.Annotateds;
import org.apache.deltaspike.core.util.AnnotationUtils;
import org.apache.deltaspike.core.util.BeanConfiguratorUtils;
import org.apache.deltaspike.core.util.BeanUtils;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ReflectionUtils;
import org.apache.deltaspike.partialbean.api.PartialBeanBinding;
import org.apache.deltaspike.proxy.api.DeltaSpikeProxyBeanConfigurator;

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

    public <X> void findInvocationHandlerBindings(@Observes ProcessAnnotatedType<X> pat)
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
                    boolean added = createPartialBean(partialBeanClass, descriptor, afterBeanDiscovery, beanManager);
                    if (added)
                    {
                        createPartialProducersDefinedIn(afterBeanDiscovery, beanManager, partialBeanClass);
                    }
                }
            }
        }

        this.descriptors.clear();
    }

    protected <T> boolean createPartialBean(Class<T> beanClass, PartialBeanDescriptor descriptor,
            AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        if (descriptor.getHandler() == null)
        {
            afterBeanDiscovery.addDefinitionError(new IllegalStateException("A class which implements "
                    + InvocationHandler.class.getName()
                    + " and is annotated with @" + descriptor.getBinding().getName()
                    + " is needed as a handler for " + beanClass.getName()
                    + ". See the documentation about @" + PartialBeanBinding.class.getName() + "."));

            return false;
        }

        AnnotatedType<T> annotatedType = beanManager.createAnnotatedType(beanClass);

        BeanConfigurator<T> beanConfigurator = afterBeanDiscovery.addBean();
        BeanConfiguratorUtils.read(beanManager, beanConfigurator, annotatedType)
            .beanClass(beanClass);
        beanConfigurator.id(PartialBeanBinding.class.getName() + ":" + Annotateds.createTypeId(annotatedType));

        new DeltaSpikeProxyBeanConfigurator(beanClass,
                descriptor.getHandler(),
                PartialBeanProxyFactory.getInstance(),
                beanManager,
                beanConfigurator)
            .delegateCreateWith()
            .delegateDestroyWith();

        return true;
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

    /*
     * logic for partial-producers
     */

    private void createPartialProducersDefinedIn(
            AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager, Class currentClass)
    {
        while (currentClass != null && !Object.class.getName().equals(currentClass.getName()))
        {
            for (Class interfaceClass : currentClass.getInterfaces())
            {
                if (interfaceClass.getName().startsWith("java.")
                        || interfaceClass.getName().startsWith("javax.")
                        || interfaceClass.getName().startsWith("jakarta."))
                {
                    continue;
                }

                createPartialProducersDefinedIn(afterBeanDiscovery, beanManager, interfaceClass);
            }

            for (Method currentMethod : currentClass.getDeclaredMethods())
            {
                if (currentMethod.isAnnotationPresent(Produces.class))
                {
                    if (currentMethod.getParameterTypes().length > 0)
                    {
                        afterBeanDiscovery.addDefinitionError(
                            new IllegalStateException(
                                "Producer-methods in partial-beans currently don't support injection-points. " +
                                "Please remove the parameters from " +
                                currentMethod.toString() + " in " + currentClass.getName()));
                    }

                    Class<? extends Annotation> scopeClass =
                        extractScope(currentMethod.getDeclaredAnnotations(), beanManager);

                    Class<?> producerResultType = currentMethod.getReturnType();

                    Set<Annotation> qualifiers = extractQualifiers(currentMethod.getDeclaredAnnotations(), beanManager);

                    final Class partialBeanClass = currentClass;

                    afterBeanDiscovery.addBean()
                            .beanClass(producerResultType)
                            .types(Object.class, producerResultType)
                            .qualifiers(qualifiers)
                            .scope(scopeClass)
                            .id(createPartialProducerId(currentClass, currentMethod, qualifiers))
                            .produceWith(e ->
                                {
                                    Object instance = BeanProvider.getContextualReference(partialBeanClass);
                                    return ReflectionUtils.invokeMethod(instance, currentMethod, Object.class, false); 
                                });
                }
            }

            currentClass = currentClass.getSuperclass();
        }
    }

    private Set<Annotation> extractQualifiers(Annotation[] annotations, BeanManager beanManager)
    {
        Set<Annotation> result = BeanUtils.getQualifiers(beanManager, annotations);

        if (result.isEmpty())
        {
            result.add(Default.Literal.INSTANCE);
        }
        return result;
    }

    private Class<? extends Annotation> extractScope(Annotation[] annotations, BeanManager beanManager)
    {
        for (Annotation annotation : annotations)
        {
            if (beanManager.isScope(annotation.annotationType()))
            {
                return annotation.annotationType();
            }
        }
        return Dependent.class;
    }

    private String createPartialProducerId(Class currentClass, Method currentMethod, Set<Annotation> qualifiers)
    {
        int qualifierHashCode = 0;
        for (Annotation qualifier : qualifiers)
        {
            qualifierHashCode += AnnotationUtils.getQualifierHashCode(qualifier);
        }
        return "PartialProducer#" + currentClass.getName() + "#" + currentMethod.getName() + "#" + qualifierHashCode;
    }
}
