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

import java.util.Collection;
import java.util.HashSet;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessProducerMethod;

import org.apache.deltaspike.core.api.literal.MessageBundleLiteral;
import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.spi.activation.Deactivatable;

public class MessageBundleExtension implements Extension, Deactivatable
{
    private final Collection<AnnotatedType<?>> messageBundleTypes;
    private Bean<Object> bundleProducerBean;

    public MessageBundleExtension()
    {
        this.messageBundleTypes = new HashSet<AnnotatedType<?>>();
    }

    void detectInterfaces(@Observes ProcessAnnotatedType<?> event,
            BeanManager beanManager)
    {
        AnnotatedType<?> type = event.getAnnotatedType();
        if (type.isAnnotationPresent(MessageBundle.class))
        {
            messageBundleTypes.add(type);
        }
    }

    // according to the Java EE 6 javadoc (the authority according to the powers
    // that be),
    // this is the correct order of type parameters
    void detectProducers(
            @Observes ProcessProducerMethod<Object, TypedMessageBundleProducer> event)
    {
        captureProducers(event.getAnnotatedProducerMethod(), event.getBean());
    }

    // according to JSR-299 spec, this is the correct order of type parameters
    @Deprecated
    void detectProducersInverted(
            @Observes ProcessProducerMethod<TypedMessageBundleProducer, Object> event)
    {
        captureProducers(event.getAnnotatedProducerMethod(), event.getBean());
    }

    @SuppressWarnings("unchecked")
    void captureProducers(AnnotatedMethod<?> method, Bean<?> bean)
    {
        if (method.isAnnotationPresent(TypedMessageBundle.class))
        {
            this.bundleProducerBean = (Bean<Object>) bean;
        }
    }

    void installBeans(@Observes AfterBeanDiscovery event,
            BeanManager beanManager)
    {
        for (AnnotatedType<?> type : messageBundleTypes)
        {
            event.addBean(createMessageBundleBean(bundleProducerBean, type,
                    beanManager));
        }
    }

    private static <T> Bean<T> createMessageBundleBean(Bean<Object> delegate,
            AnnotatedType<T> type, BeanManager beanManager)
    {
        return new NarrowingBeanBuilder<T>(delegate, beanManager)
                .readFromType(type).types(type.getBaseType(), Object.class)
                .addQualifier(new MessageBundleLiteral()).create();
    }

    void cleanup(@Observes AfterDeploymentValidation event)
    {
        this.messageBundleTypes.clear();
    }

}
