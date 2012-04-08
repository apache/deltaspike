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
package org.apache.deltaspike.core.impl.message;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessProducerMethod;

import org.apache.deltaspike.core.api.message.Message;
import org.apache.deltaspike.core.api.message.MessageContext;
import org.apache.deltaspike.core.api.message.annotation.MessageBundle;
import org.apache.deltaspike.core.api.message.annotation.MessageTemplate;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

public class MessageBundleExtension implements Extension, Deactivatable
{
    private final Collection<AnnotatedType<?>> messageBundleTypes = new HashSet<AnnotatedType<?>>();
    private Bean<Object> bundleProducerBean;

    private Boolean isActivated = null;

    @SuppressWarnings("UnusedDeclaration")
    protected void init(@Observes BeforeBeanDiscovery afterBeanDiscovery)
    {
        initActivation();
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void detectInterfaces(@Observes ProcessAnnotatedType<?> event)
    {
        if (!this.isActivated)
        {
            return;
        }

        AnnotatedType<?> type = event.getAnnotatedType();

        if (type.isAnnotationPresent(MessageBundle.class))
        {
            validateMessageBundle(type.getJavaClass());

            messageBundleTypes.add(type);
        }
    }

    private void validateMessageBundle(Class<?> currentClass)
    {
        for (Method currentMethod : currentClass.getDeclaredMethods())
        {
            if (!currentMethod.isAnnotationPresent(MessageTemplate.class))
            {
                continue;
            }
            
            if (String.class.isAssignableFrom(currentMethod.getReturnType()))
            {
                continue;
            }

            if (Message.class.isAssignableFrom(currentMethod.getReturnType()))
            {
                validateMessageContextAwareMethod(currentMethod);
            }
            else
            {
                throw new IllegalStateException(
                        currentMethod.getReturnType().getName() + " isn't supported. Details: " +
                        currentMethod.getDeclaringClass().getName() + "#" + currentMethod.getName() +
                        " only " + String.class.getName() + " or " + Message.class.getName());
            }
        }
    }

    private void validateMessageContextAwareMethod(Method currentMethod)
    {
        for (Class currentParameterType : currentMethod.getParameterTypes())
        {
            if (MessageContext.class.isAssignableFrom(currentParameterType))
            {
                return;
            }
        }

        throw new IllegalStateException("No " + MessageContext.class.getName() + " parameter found at: " +
                currentMethod.getDeclaringClass().getName() + "#" + currentMethod.getName() +
                ". That is required for return-type " + Message.class.getName());
    }

    // according to the Java EE 6 javadoc (the authority according to the powers
    // that be),
    // this is the correct order of type parameters
    @SuppressWarnings("UnusedDeclaration")
    protected void detectProducers(@Observes ProcessProducerMethod<Object, TypedMessageBundleProducer> event)
    {
        if (!this.isActivated)
        {
            return;
        }

        captureProducers(event.getAnnotatedProducerMethod(), event.getBean());
    }

    // according to JSR-299 spec, this is the correct order of type parameters
    //X TODO re-visit it
    @Deprecated
    @SuppressWarnings("UnusedDeclaration")
    protected void detectProducersInverted(@Observes ProcessProducerMethod<TypedMessageBundleProducer, Object> event)
    {
        if (!this.isActivated)
        {
            return;
        }

        captureProducers(event.getAnnotatedProducerMethod(), event.getBean());
    }

    @SuppressWarnings("unchecked")
    protected void captureProducers(AnnotatedMethod<?> method, Bean<?> bean)
    {
        if (method.isAnnotationPresent(TypedMessageBundle.class))
        {
            this.bundleProducerBean = (Bean<Object>) bean;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void installMessageBundleProducerBeans(@Observes AfterBeanDiscovery event, BeanManager beanManager)
    {
        for (AnnotatedType<?> type : messageBundleTypes)
        {
            event.addBean(createMessageBundleBean(bundleProducerBean, type, beanManager));
        }
    }

    private static <T> Bean<T> createMessageBundleBean(Bean<Object> delegate,
                                                       AnnotatedType<T> annotatedType,
                                                       BeanManager beanManager)
    {
        return new NarrowingBeanBuilder<T>(delegate, beanManager)
                .readFromType(annotatedType)
                //X TODO re-visit type.getBaseType() in combination with #addQualifier
                .types(annotatedType.getJavaClass(), Object.class)
                .create();
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void cleanup(@Observes AfterDeploymentValidation event)
    {
        this.messageBundleTypes.clear();
    }

    protected void initActivation()
    {
        if (isActivated == null)
        {
            isActivated = ClassDeactivationUtils.isActivated(getClass());
        }
    }
}
