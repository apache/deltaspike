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

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.apache.deltaspike.proxy.api.DeltaSpikeProxyContextualLifecycle;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.faces.convert.Converter;
import javax.faces.validator.Validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class ConverterAndValidatorProxyExtension implements Extension, Deactivatable
{
    private static final Logger LOG = Logger.getLogger(ConverterAndValidatorProxyExtension.class.getName());

    private Boolean isActivated = true;
    private Set<Class<?>> classesToProxy = new HashSet<Class<?>>();

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        this.isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    @SuppressWarnings("UnusedDeclaration")
    public <X> void findConverterAndValidatorsWhichNeedProxiesForDependencyInjectionSupport(
            @Observes ProcessAnnotatedType<X> pat, BeanManager beanManager)
    {
        if (!this.isActivated)
        {
            return;
        }

        Class<X> beanClass = pat.getAnnotatedType().getJavaClass();

        if (!(Converter.class.isAssignableFrom(beanClass) || (Validator.class.isAssignableFrom(beanClass))))
        {
            return;
        }

        Bean<X> bean = new BeanBuilder<X>(beanManager).readFromType(pat.getAnnotatedType()).create();
        // veto normal converters/validators -> they will get excluded from the special handling later on
        if (!hasInjectionPoints(bean) && !hasNormalScopeAnnotation(bean, beanManager))
        {
            pat.veto();
            return;
        }

        // converters/validators without properties for tags, will be handled by the corresponding manual wrapper
        if (!hasPublicProperty(beanClass))
        {
            return;
        }

        if (!(Modifier.isFinal(beanClass.getModifiers())))
        {
            this.classesToProxy.add(beanClass);
            pat.veto();
        }
        else
        {
            LOG.warning("To use dependency-injection in converters/validators with properties, " +
                    "you they aren't allowed to be 'final'.");
        }
    }

    protected <X> boolean hasInjectionPoints(Bean<X> bean)
    {
        return !bean.getInjectionPoints().isEmpty();
    }

    protected <X> boolean hasNormalScopeAnnotation(Bean<X> bean, BeanManager beanManager)
    {
        Class<? extends Annotation> scopeAnnotationClass = bean.getScope();
        return scopeAnnotationClass != null && beanManager.isNormalScope(scopeAnnotationClass);
    }

    protected <X> boolean hasPublicProperty(Class<X> beanClass)
    {
        for (Method currentMethod : beanClass.getMethods())
        {
            if (currentMethod.getName().startsWith("set") && currentMethod.getName().length() > 3
                    && currentMethod.getParameterTypes().length == 1 &&
                    hasGetterMethod(beanClass, currentMethod.getName().substring(3)))
            {
                return true;
            }
        }
        return false;
    }

    protected boolean hasGetterMethod(Class beanClass, String name)
    {
        try
        {
            if (beanClass.getMethod("get" + name) != null || beanClass.getMethod("is" + name) != null)
            {
                return true;
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return false;
    }

    public <X> void createBeans(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
    {
        if (!this.isActivated)
        {
            return;
        }

        for (Class<?> originalClass : this.classesToProxy)
        {
            Bean bean = createBean(originalClass, beanManager);

            if (bean != null)
            {
                afterBeanDiscovery.addBean(bean);
            }
        }

        this.classesToProxy.clear();
    }

    protected <T> Bean<T> createBean(Class<T> beanClass, BeanManager beanManager)
    {
        Class<? extends InvocationHandler> invocationHandlerClass =
                Converter.class.isAssignableFrom(beanClass) ?
                        ConverterInvocationHandler.class : ValidatorInvocationHandler.class;

        AnnotatedType<T> annotatedType = new AnnotatedTypeBuilder<T>().readFromType(beanClass).create();

        DeltaSpikeProxyContextualLifecycle lifecycle = new DeltaSpikeProxyContextualLifecycle(beanClass,
                invocationHandlerClass, ConverterAndValidatorProxyFactory.getInstance(), beanManager);

        BeanBuilder<T> beanBuilder = new BeanBuilder<T>(beanManager)
                .readFromType(annotatedType)
                .passivationCapable(true)
                .beanLifecycle(lifecycle);

        return beanBuilder.create();
    }
}
