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

package org.apache.deltaspike.core.impl.exception.control;

import org.apache.deltaspike.core.api.exception.control.BeforeHandles;
import org.apache.deltaspike.core.api.exception.control.CaughtException;
import org.apache.deltaspike.core.api.exception.control.HandlerMethod;
import org.apache.deltaspike.core.api.exception.control.Handles;
import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.metadata.builder.ImmutableInjectionPoint;
import org.apache.deltaspike.core.api.metadata.builder.InjectableMethod;
import org.apache.deltaspike.core.util.BeanUtils;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link HandlerMethod}.
 *
 * @param <T> Type of the exception this handler handles.
 */
public class HandlerMethodImpl<T extends Throwable> implements HandlerMethod<T>
{
    private final Class<?> beanClass;
    private Bean<?> bean;
    private final Set<Annotation> qualifiers;
    private final Type exceptionType;
    private final AnnotatedMethod<?> handler;
    private final boolean before;
    private final int ordinal;
    private final Method javaMethod;
    private final AnnotatedParameter<?> handlerParameter;
    private final Set<InjectionPoint> injectionPoints;
    private BeanManager beanManager;

    /**
     * Sole Constructor.
     *
     * @param method found handler
     * @param bm     active BeanManager
     * @throws IllegalArgumentException if method is null, has no params or first param is not annotated with
     *                                  {@link Handles} or {@link BeforeHandles}
     */
    public HandlerMethodImpl(final AnnotatedMethod<?> method, final BeanManager bm)
    {
        if (!HandlerMethodImpl.isHandler(method))
        {
            throw new IllegalArgumentException(MessageFormat.format("{0} is not a valid handler", method));
        }

        this.beanManager = bm;

        final Set<Annotation> tmpQualifiers = new HashSet<Annotation>();

        this.handler = method;
        this.javaMethod = method.getJavaMember();

        this.handlerParameter = findHandlerParameter(method);

        if (!this.handlerParameter.isAnnotationPresent(Handles.class)
                && !this.handlerParameter.isAnnotationPresent(BeforeHandles.class))
        {
            throw new IllegalArgumentException("Method is not annotated with @Handles or @BeforeHandles");
        }

        this.before = this.handlerParameter.getAnnotation(BeforeHandles.class) != null;
        this.ordinal = this.handlerParameter.getAnnotation(Handles.class).ordinal();
        tmpQualifiers.addAll(BeanUtils.getQualifiers(bm, this.handlerParameter.getAnnotations()));

        if (tmpQualifiers.isEmpty())
        {
            tmpQualifiers.add(new AnyLiteral());
        }

        this.qualifiers = tmpQualifiers;
        this.beanClass = method.getJavaMember().getDeclaringClass();
        this.exceptionType = ((ParameterizedType) this.handlerParameter.getBaseType()).getActualTypeArguments()[0];
        this.injectionPoints = new HashSet<InjectionPoint>(method.getParameters().size() - 1);

        for (AnnotatedParameter<?> param : method.getParameters())
        {
            if (!param.equals(this.handlerParameter))
            {
                this.injectionPoints.add(new ImmutableInjectionPoint(param, bm, this.getBean(bm), false, false));
            }
        }
    }

    /**
     * Determines if the given method is a handler by looking for the {@link Handles} annotation on a parameter.
     *
     * @param method method to search
     * @return true if {@link Handles} is found, false otherwise
     */
    public static boolean isHandler(final AnnotatedMethod<?> method)
    {
        if (method == null)
        {
            throw new IllegalArgumentException("Method must not be null");
        }

        for (AnnotatedParameter<?> param : method.getParameters())
        {
            if (param.isAnnotationPresent(Handles.class) || param.isAnnotationPresent(BeforeHandles.class))
            {
                return true;
            }
        }

        return false;
    }

    public static AnnotatedParameter<?> findHandlerParameter(final AnnotatedMethod<?> method)
    {
        if (!isHandler(method))
        {
            throw new IllegalArgumentException("Method is not a valid handler");
        }

        AnnotatedParameter<?> returnParam = null;

        for (AnnotatedParameter<?> param : method.getParameters())
        {
            if (param.isAnnotationPresent(Handles.class) || param.isAnnotationPresent(BeforeHandles.class))
            {
                returnParam = param;
                break;
            }
        }

        return returnParam;
    }

    public Class<?> getBeanClass()
    {
        return this.beanClass;
    }

    public synchronized Bean<?> getBean(BeanManager bm)
    {
        if (this.bean == null)
        {
            this.bean = bm.resolve(bm.getBeans(this.beanClass));
        }
        return this.bean;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Annotation> getQualifiers()
    {
        return Collections.unmodifiableSet(this.qualifiers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getExceptionType()
    {
        return this.exceptionType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(final CaughtException<T> event)
    {
        CreationalContext<?> ctx = null;
        try
        {
            ctx = beanManager.createCreationalContext(null);
            Object handlerInstance = beanManager.getReference(this.getBean(beanManager), this.beanClass, ctx);
            InjectableMethod<?> im = createInjectableMethod(this.handler, this.getBean(beanManager), beanManager);
            im.invoke(handlerInstance, ctx, new OutboundParameterValueRedefiner(event, beanManager, this));
        }
        finally
        {
            if (ctx != null)
            {
                ctx.release();
            }
        }
    }

    private <X> InjectableMethod<X> createInjectableMethod(AnnotatedMethod<X> handlerMethod,
                                                           Bean<?> bean, BeanManager manager)
    {
        return new InjectableMethod<X>(handlerMethod, bean, manager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBefore()
    {
        return this.before;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrdinal()
    {
        return this.ordinal;
    }

    public Method getJavaMethod()
    {
        return this.javaMethod;
    }

    public AnnotatedParameter<?> getHandlerParameter()
    {
        return this.handlerParameter;
    }

    public Set<InjectionPoint> getInjectionPoints()
    {
        return new HashSet<InjectionPoint>(this.injectionPoints);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        HandlerMethod<?> that = (HandlerMethod<?>) o;

        if (!qualifiers.equals(that.getQualifiers()))
        {
            return false;
        }
        if (!exceptionType.equals(that.getExceptionType()))
        {
            return false;
        }
        return ordinal == that.getOrdinal();

    }

    @Override
    public int hashCode()
    {
        int result = beanClass.hashCode();
        result = 5 * result + qualifiers.hashCode();
        result = 5 * result + exceptionType.hashCode();
        result = 5 * result + ordinal;
        result = 5 * result + javaMethod.hashCode();
        result = 5 * result + handlerParameter.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return new StringBuilder("Qualifiers: ").append(this.qualifiers).append(" ")
                .append("Handles Type: ").append(this.exceptionType).append(" ")
                .append("Before: ").append(this.before).append(" ")
                .append("Precedence: ").append(this.ordinal).append(" ")
                .append(this.handler.toString()).toString();
    }
}
