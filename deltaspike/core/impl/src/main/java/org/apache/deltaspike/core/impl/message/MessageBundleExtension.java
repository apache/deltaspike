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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.deltaspike.core.api.message.Message;
import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;

/**
 * Extension for handling {@link MessageBundle}s.
 *
 * @see MessageBundle
 * @see MessageTemplate
 */
public class MessageBundleExtension implements Extension, Deactivatable
{
    private final Collection<AnnotatedType<?>> messageBundleTypes = new HashSet<AnnotatedType<?>>();

    private List<String> deploymentErrors = new ArrayList<String>();

    private Boolean isActivated = true;

    @SuppressWarnings("UnusedDeclaration")
    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void detectInterfaces(@Observes ProcessAnnotatedType processAnnotatedType)
    {
        if (!isActivated)
        {
            return;
        }

        AnnotatedType<?> type = processAnnotatedType.getAnnotatedType();

        if (type.isAnnotationPresent(MessageBundle.class))
        {
            if (validateMessageBundle(type.getJavaClass()))
            {
                messageBundleTypes.add(type);
            }
        }
    }

    /**
     * @return <code>true</code> if all is well
     */
    private boolean validateMessageBundle(Class<?> currentClass)
    {
        boolean ok = true;

        // sanity check: annotated class must be an Interface
        if (!currentClass.isInterface())
        {
            deploymentErrors.add("@MessageBundle must only be used on Interfaces, but got used on class " +
                    currentClass.getName());
            return false;
        }

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
                continue;
            }

            deploymentErrors.add(currentMethod.getReturnType().getName() + " isn't supported. Details: " +
                    currentMethod.getDeclaringClass().getName() + "#" + currentMethod.getName() +
                    " only " + String.class.getName() + " or " + Message.class.getName());
            ok = false;
        }

        return ok;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void installMessageBundleProducerBeans(@Observes AfterBeanDiscovery abd, BeanManager beanManager)
    {
        if (!deploymentErrors.isEmpty())
        {
            abd.addDefinitionError(new IllegalArgumentException("The following MessageBundle problems where found: " +
                    Arrays.toString(deploymentErrors.toArray())));
            return;
        }

        for (AnnotatedType<?> mbType : messageBundleTypes)
        {
            abd.addBean()
                .createWith(cc -> {
                        final Bean<?> invocationHandlerBean = beanManager.resolve(
                            beanManager.getBeans(MessageBundleInvocationHandler.class));

                        return createMessageBundleProxy(mbType.getJavaClass(),
                            (MessageBundleInvocationHandler)
                                beanManager.getReference(invocationHandlerBean, MessageBundleInvocationHandler.class, cc));
                    })
                .types(mbType.getJavaClass(), Object.class, Serializable.class)
                .addQualifier(Default.Literal.INSTANCE)
                .scope(ApplicationScoped.class) // needs to be a normalscope due to a bug in older Weld versions
                .id("MessageBundleBean#" + mbType.getJavaClass().getName());
        }
    }


    @SuppressWarnings("UnusedDeclaration")
    protected void cleanup(@Observes AfterDeploymentValidation afterDeploymentValidation)
    {
        messageBundleTypes.clear();
    }

    private <T> T createMessageBundleProxy(Class<T> type, MessageBundleInvocationHandler handler)
    {
        return type.cast(Proxy.newProxyInstance(ClassUtils.getClassLoader(null),
            new Class<?>[]{type, Serializable.class}, handler));
    }
}
