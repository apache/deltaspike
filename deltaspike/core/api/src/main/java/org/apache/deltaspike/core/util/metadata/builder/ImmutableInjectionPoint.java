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
package org.apache.deltaspike.core.util.metadata.builder;

import org.apache.deltaspike.core.util.BeanUtils;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * <p>
 * A base class for implementing {@link InjectionPoint}. The attributes are
 * immutable, and collections are defensively copied on instantiation.
 * </p>
 */
public class ImmutableInjectionPoint implements InjectionPoint
{

    private final Annotated annotated;
    private final Member member;
    private final Bean<?> declaringBean;
    private final Set<Annotation> qualifiers;
    private final Type type;
    private final boolean isTransient;
    private final boolean delegate;

    /**
     * Instantiate a new {@link InjectionPoint} based upon an
     * {@link AnnotatedField}.
     *
     * @param field         the field for which to create the injection point
     * @param qualifiers    the qualifiers on the injection point
     * @param declaringBean the declaringBean declaring the injection point
     * @param isTransient   <code>true</code> if the injection point is transient
     * @param delegate      <code>true</code> if the injection point is a delegate
     *                      injection point on a decorator
     */
    public ImmutableInjectionPoint(AnnotatedField<?> field, Set<Annotation> qualifiers, Bean<?> declaringBean,
                                   boolean isTransient, boolean delegate)
    {
        annotated = field;
        member = field.getJavaMember();
        this.qualifiers = new HashSet<Annotation>(qualifiers);
        type = field.getJavaMember().getGenericType();
        this.isTransient = isTransient;
        this.delegate = delegate;
        this.declaringBean = declaringBean;
    }

    /**
     * Instantiate a new {@link InjectionPoint} based upon an
     * {@link AnnotatedField}, reading the qualifiers from the annotations
     * declared on the field.
     *
     * @param field         the field for which to create the injection point
     * @param declaringBean the declaringBean declaring the injection point
     * @param isTransient   <code>true</code> if the injection point is transient
     * @param delegate      <code>true</code> if the injection point is a delegate
     *                      injection point on a decorator
     */
    public ImmutableInjectionPoint(AnnotatedField<?> field, BeanManager beanManager, Bean<?> declaringBean,
                                   boolean isTransient, boolean delegate)
    {
        annotated = field;
        member = field.getJavaMember();
        qualifiers = BeanUtils.getQualifiers(beanManager, field.getAnnotations());
        type = field.getJavaMember().getGenericType();
        this.isTransient = isTransient;
        this.delegate = delegate;
        this.declaringBean = declaringBean;
    }

    /**
     * Instantiate a new {@link InjectionPoint} based upon an
     * {@link AnnotatedParameter}.
     *
     * @param parameter     the parameter for which to create the injection point
     * @param qualifiers    the qualifiers on the injection point
     * @param declaringBean the declaringBean declaring the injection point
     * @param isTransient   <code>true</code> if the injection point is transient
     * @param delegate      <code>true</code> if the injection point is a delegate
     *                      injection point on a decorator
     */
    public ImmutableInjectionPoint(AnnotatedParameter<?> parameter, Set<Annotation> qualifiers, Bean<?> declaringBean,
                                   boolean isTransient, boolean delegate)
    {
        annotated = parameter;
        member = parameter.getDeclaringCallable().getJavaMember();
        this.qualifiers = new HashSet<Annotation>(qualifiers);
        this.isTransient = isTransient;
        this.delegate = delegate;
        this.declaringBean = declaringBean;
        type = parameter.getBaseType();
    }

    /**
     * Instantiate a new {@link InjectionPoint} based upon an
     * {@link AnnotatedParameter}, reading the qualifiers from the annotations
     * declared on the parameter.
     *
     * @param parameter     the parameter for which to create the injection point
     * @param declaringBean the declaringBean declaring the injection point
     * @param isTransient   <code>true</code> if the injection point is transient
     * @param delegate      <code>true</code> if the injection point is a delegate
     *                      injection point on a decorator
     */
    public ImmutableInjectionPoint(AnnotatedParameter<?> parameter, BeanManager beanManager, Bean<?> declaringBean,
                                   boolean isTransient, boolean delegate)
    {
        annotated = parameter;
        member = parameter.getDeclaringCallable().getJavaMember();
        qualifiers = BeanUtils.getQualifiers(beanManager, parameter.getAnnotations());
        this.isTransient = isTransient;
        this.delegate = delegate;
        this.declaringBean = declaringBean;
        type = parameter.getBaseType();
    }

    public Annotated getAnnotated()
    {
        return annotated;
    }

    public Bean<?> getBean()
    {
        return declaringBean;
    }

    public Member getMember()
    {
        return member;
    }

    public Set<Annotation> getQualifiers()
    {
        return unmodifiableSet(qualifiers);
    }

    public Type getType()
    {
        return type;
    }

    public boolean isDelegate()
    {
        return delegate;
    }

    public boolean isTransient()
    {
        return isTransient;
    }

}
