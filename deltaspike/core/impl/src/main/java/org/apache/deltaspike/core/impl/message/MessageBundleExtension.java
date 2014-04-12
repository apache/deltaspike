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

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import javax.inject.Named;

import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.message.Message;
import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;
import org.apache.deltaspike.core.util.bean.ImmutableBeanWrapper;
import org.apache.deltaspike.core.util.bean.ImmutablePassivationCapableBeanWrapper;
import org.apache.deltaspike.core.util.bean.WrappingBeanBuilder;
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
    private Bean<Object> bundleProducerBean;
    private Bean<Object> namedBundleProducerBean;
    private NamedTypedMessageBundle namedTypedMessageBundle = new NamedTypedMessageBundleLiteral();
    private boolean elSupportEnabled;

    private List<String> deploymentErrors = new ArrayList<String>();

    private Boolean isActivated = true;

    @SuppressWarnings("UnusedDeclaration")
    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        isActivated = ClassDeactivationUtils.isActivated(getClass());
        elSupportEnabled = ClassDeactivationUtils.isActivated(NamedMessageBundleInvocationHandler.class);
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
            if (isValidMethod(currentMethod))
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

    private boolean isValidMethod(Method currentMethod)
    {
        if (!currentMethod.isAnnotationPresent(MessageTemplate.class) || String.class
                .isAssignableFrom(currentMethod.getReturnType()) || Message.class
                .isAssignableFrom(currentMethod.getReturnType()))
        {
            return true;
        }
        return false;
    }

    /**
     * Part of a workaround for very old CDI containers. The spec originally had a
     * mismatch in the generic parameters of ProcessProducerMethod between the JavaDoc
     * and the spec PDF.
     * <p/>
     * According to the Java EE 6 javadoc (the authority according to the powers
     * that be), this is the correct order of type parameters.
     *
     * @see #detectProducersInverted(javax.enterprise.inject.spi.ProcessProducerMethod)
     */
    @SuppressWarnings("UnusedDeclaration")
    protected void detectProducers(@Observes ProcessProducerMethod<Object, TypedMessageBundleProducer> event)
    {
        if (!isActivated)
        {
            return;
        }

        captureProducers(event.getAnnotatedProducerMethod(), event.getBean());
    }

    /**
     * Part of a workaround for very old CDI containers. The spec originally had a
     * mismatch in the generic parameters of ProcessProducerMethod between the JavaDoc
     * and the spec PDF.
     * <p/>
     * According to the old JSR-299 spec wording, this is the correct order of type parameters.
     * This is now fixed in the spec as of today, but old containers might still fire it!
     *
     * @see #detectProducersInverted(javax.enterprise.inject.spi.ProcessProducerMethod)
     */
    @Deprecated
    @SuppressWarnings("UnusedDeclaration")
    protected void detectProducersInverted(@Observes ProcessProducerMethod<TypedMessageBundleProducer, Object> event)
    {
        if (!isActivated)
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
            bundleProducerBean = (Bean<Object>) bean;
        }
        else if (method.isAnnotationPresent(NamedTypedMessageBundle.class))
        {
            namedBundleProducerBean = (Bean<Object>) bean;
        }
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

        for (AnnotatedType<?> type : messageBundleTypes)
        {
            abd.addBean(createMessageBundleBean(bundleProducerBean, type, beanManager));

            if (this.elSupportEnabled)
            {
                Bean<?> namedBean = createNamedMessageBundleBean(namedBundleProducerBean, type, beanManager);
                if (namedBean.getName() != null)
                {
                    abd.addBean(namedBean);
                }
            }
        }
    }

    private <T> Bean<T> createMessageBundleBean(Bean<Object> delegate,
                                                AnnotatedType<T> annotatedType,
                                                BeanManager beanManager)
    {
        WrappingBeanBuilder<T> beanBuilder = new WrappingBeanBuilder<T>(delegate, beanManager)
                .readFromType(annotatedType);

        if (this.elSupportEnabled)
        {
            /*see namedBundleProducerBean - a producer without injection-point is needed*/
            beanBuilder.name(null);
        }
        //X TODO re-visit type.getBaseType() in combination with #addQualifier
        beanBuilder.types(annotatedType.getJavaClass(), Object.class, Serializable.class);
        beanBuilder.passivationCapable(true);
        beanBuilder.id("MessageBundleBean#" + annotatedType.getJavaClass().getName());

        return beanBuilder.create();
    }

    private <T> Bean<T> createNamedMessageBundleBean(Bean<Object> delegate,
                                                     AnnotatedType<T> annotatedType,
                                                     BeanManager beanManager)
    {
        WrappingBeanBuilder<T> beanBuilder = new WrappingBeanBuilder<T>(delegate, beanManager)
        {
            @Override
            public ImmutableBeanWrapper<T> create()
            {
                final ImmutableBeanWrapper<T> result = super.create();

                String beanName = createBeanName(result.getTypes());

                Set<Annotation> qualifiers = new HashSet<Annotation>();
                qualifiers.add(new AnyLiteral());
                qualifiers.add(namedTypedMessageBundle);

                if (isPassivationCapable())
                {
                    return new ImmutablePassivationCapableBeanWrapper<T>(result,
                            beanName, qualifiers, result.getScope(),
                            result.getStereotypes(), result.getTypes(), result.isAlternative(),
                            result.isNullable(), result.toString(), ((PassivationCapable) result).getId())
                    {
                        @Override
                        public T create(CreationalContext<T> creationalContext)
                        {
                            MessageBundleContext.setBean(result);

                            try
                            {
                                return super.create(creationalContext);
                            }
                            finally
                            {
                                MessageBundleContext.reset();
                            }
                        }
                    };
                }
                else
                {
                    return new ImmutableBeanWrapper<T>(result,
                            beanName, qualifiers, result.getScope(),
                            result.getStereotypes(), result.getTypes(), result.isAlternative(),
                            result.isNullable(), result.toString())
                    {
                        @Override
                        public T create(CreationalContext<T> creationalContext)
                        {
                            MessageBundleContext.setBean(result);
                            try
                            {
                                return super.create(creationalContext);
                            }
                            finally
                            {
                                MessageBundleContext.reset();
                            }
                        }
                    };
                }
            }

            private String createBeanName(Set<Type> types)
            {
                for (Object type : types)
                {
                    if (type instanceof Class)
                    {
                        Named namedAnnotation = ((Class<?>) type).getAnnotation(Named.class);

                        if (namedAnnotation == null)
                        {
                            continue;
                        }

                        String result = namedAnnotation.value();
                        if (!"".equals(result))
                        {
                            return result;
                        }
                        return Introspector.decapitalize(((Class<?>) type).getSimpleName());
                    }
                }
                return null;
            }
        };
        beanBuilder.readFromType(annotatedType);

        //X TODO re-visit type.getBaseType() in combination with #addQualifier
        beanBuilder.types(annotatedType.getJavaClass(), Object.class, Serializable.class);
        beanBuilder.passivationCapable(true);
        beanBuilder.id("NamedMessageBundleBean#" + annotatedType.getJavaClass().getName());

        return beanBuilder.create();
    }


    @SuppressWarnings("UnusedDeclaration")
    protected void cleanup(@Observes AfterDeploymentValidation afterDeploymentValidation)
    {
        messageBundleTypes.clear();
    }
}
