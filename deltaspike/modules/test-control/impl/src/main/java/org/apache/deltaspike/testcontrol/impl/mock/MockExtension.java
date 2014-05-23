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
package org.apache.deltaspike.testcontrol.impl.mock;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ServiceUtils;
import org.apache.deltaspike.testcontrol.spi.mock.MockFilter;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MockExtension implements Extension, Deactivatable
{
    private Boolean isActivated = true;
    private List<MockFilter> mockFilters;

    protected void init(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        isActivated = ClassDeactivationUtils.isActivated(getClass());
        mockFilters = ServiceUtils.loadServiceImplementations(MockFilter.class);
    }

    public <X> void onProcessInjectionTarget(@Observes ProcessInjectionTarget<X> processInjectionTarget,
                                             BeanManager beanManager)
    {
        if (!isActivated)
        {
            return;
        }

        for (MockFilter mockFilter : mockFilters)
        {
            if (!mockFilter.isMockedImplementationSupported(beanManager, processInjectionTarget.getAnnotatedType()))
            {
                return;
            }
        }

        List<Annotation> qualifiers = new ArrayList<Annotation>();
        for (Annotation annotation : processInjectionTarget.getAnnotatedType().getAnnotations())
        {
            if (beanManager.isQualifier(annotation.annotationType()))
            {
                qualifiers.add(annotation);
            }
        }

        Typed typed = processInjectionTarget.getAnnotatedType().getAnnotation(Typed.class);

        List<Type> foundTypes = new ArrayList<Type>();
        if (typed != null)
        {
            Collections.addAll(foundTypes, typed.value());
        }
        else
        {
            foundTypes.addAll(extractTypes(processInjectionTarget.getAnnotatedType().getJavaClass()));
        }

        if (foundTypes.isEmpty())
        {
            return;
        }

        final InjectionTarget<X> originalInjectionTarget = processInjectionTarget.getInjectionTarget();
        processInjectionTarget.setInjectionTarget(new MockAwareInjectionTargetWrapper<X>(
            beanManager, originalInjectionTarget, foundTypes, qualifiers));
    }

    public <X, T> void onProcessProducer(@Observes ProcessProducer<X, T> processProducer, BeanManager beanManager)
    {
        if (!isActivated)
        {
            return;
        }

        for (MockFilter mockFilter : mockFilters)
        {
            if (!mockFilter.isMockedImplementationSupported(beanManager, processProducer.getAnnotatedMember()))
            {
                return;
            }
        }

        final Producer<T> originalProducer = processProducer.getProducer();
        AnnotatedMember<X> annotatedMember = processProducer.getAnnotatedMember();
        List<Annotation> qualifiers = new ArrayList<Annotation>();
        for (Annotation annotation : annotatedMember.getAnnotations())
        {
            if (beanManager.isQualifier(annotation.annotationType()))
            {
                qualifiers.add(annotation);
            }
        }

        Typed typed = annotatedMember.getAnnotation(Typed.class);

        List<Type> foundTypes = new ArrayList<Type>();
        if (typed != null)
        {
            Collections.addAll(foundTypes, typed.value());
        }
        else if (annotatedMember.getBaseType() instanceof Class)
        {
            foundTypes.addAll(extractTypes((Class)annotatedMember.getBaseType()));
        }

        if (foundTypes.isEmpty())
        {
            return;
        }

        processProducer.setProducer(new MockAwareProducerWrapper<T>(
            beanManager, originalProducer, foundTypes, qualifiers));
    }

    //logic from org.apache.deltaspike.core.util.bean.BeanBuilder
    protected List<Type> extractTypes(Class currentClass)
    {
        List<Type> result = new ArrayList<Type>();
        for (Class<?> c = currentClass; c != Object.class && c != null; c = c.getSuperclass())
        {
            result.add(c);
        }
        Collections.addAll(result, currentClass.getInterfaces());
        return result;
    }
}
