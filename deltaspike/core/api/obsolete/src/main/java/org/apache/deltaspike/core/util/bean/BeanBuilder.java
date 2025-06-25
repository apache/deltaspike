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

package org.apache.deltaspike.core.util.bean;

import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.literal.DefaultLiteral;
import org.apache.deltaspike.core.util.Annotateds;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;
import org.apache.deltaspike.core.util.metadata.builder.DelegatingContextualLifecycle;
import org.apache.deltaspike.core.util.metadata.builder.DummyInjectionTarget;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.inject.Named;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * A builder class for creating immutable beans. The builder can create
 * {@link jakarta.enterprise.inject.spi.PassivationCapable} beans.
 * </p>
 * <p/>
 * <p>
 * The builder can read from an {@link AnnotatedType} and have any attribute
 * modified. This class is not thread-safe, but the bean created by calling
 * {@link #create()} is.
 * </p>
 * <p/>
 * <p>
 * It is advised that a new bean builder is instantiated for each bean created.
 * </p>
 */
public class BeanBuilder<T>
{

    protected final BeanManager beanManager;

    protected Class<?> beanClass;
    protected String name;
    protected Set<Annotation> qualifiers;
    protected Class<? extends Annotation> scope = Dependent.class;
    protected Set<Class<? extends Annotation>> stereotypes;
    protected Set<Type> types;
    protected Set<InjectionPoint> injectionPoints;
    protected boolean alternative;
    protected boolean nullable;
    protected ContextualLifecycle<T> beanLifecycle;
    protected boolean passivationCapable;
    protected String id;
    protected String toString;

    /**
     * Instantiate a new bean builder.
     *
     * @param beanManager the bean manager to use for creating injection targets
     *                    and determining if annotations are qualifiers, scopes or
     *                    stereotypes.
     */
    public BeanBuilder(BeanManager beanManager)
    {
        this.beanManager = beanManager;
    }

    /**
     * <p>
     * Read the {@link AnnotatedType}, creating a bean from the class and it's
     * annotations.
     * </p>
     * <p/>
     * <p>
     * By default the bean lifecycle will wrap the result of calling
     * {@link BeanManager#createInjectionTarget(AnnotatedType)}.
     * </p>
     * <p/>
     * <p>
     * {@link BeanBuilder} does <em>not</em> support reading members of the class
     * to create producers or observer methods.
     * </p>
     *
     * @param type the type to read
     */
    public BeanBuilder<T> readFromType(AnnotatedType<T> type)
    {
        this.beanClass = type.getJavaClass();

        if (beanLifecycle == null)
        {
            setDefaultBeanLifecycle(type);
        }

        this.qualifiers = new HashSet<Annotation>();
        this.stereotypes = new HashSet<Class<? extends Annotation>>();
        this.types = new HashSet<Type>();
        for (Annotation annotation : type.getAnnotations())
        {
            if (beanManager.isQualifier(annotation.annotationType()))
            {
                this.qualifiers.add(annotation);
            }
            else if (beanManager.isScope(annotation.annotationType()))
            {
                this.scope = annotation.annotationType();
            }
            else if (beanManager.isStereotype(annotation.annotationType()))
            {
                this.stereotypes.add(annotation.annotationType());
            }
            if (annotation instanceof Named)
            {
                this.name = ((Named) annotation).value();
                if (name == null || name.length() == 0)
                {
                    name = createDefaultBeanName(type);
                }
            }
            if (annotation instanceof Alternative)
            {
                this.alternative = true;
            }
        }
        if (type.isAnnotationPresent(Typed.class))
        {
            Typed typed = type.getAnnotation(Typed.class);
            this.types.addAll(Arrays.asList(typed.value()));

        }
        else
        {
            for (Class<?> c = type.getJavaClass(); c != Object.class && c != null; c = c.getSuperclass())
            {
                this.types.add(c);
            }
            Collections.addAll(this.types, type.getJavaClass().getInterfaces());
            this.types.add(Object.class);
        }        

        if (qualifiers.isEmpty())
        {
            qualifiers.add(new DefaultLiteral());
        }
        qualifiers.add(new AnyLiteral());

        this.id = ImmutableBeanWrapper.class.getName() + ":" + Annotateds.createTypeId(type);
        return this;
    }

    private String createDefaultBeanName(AnnotatedType<T> type)
    {
        Class<T> javaClass = type.getJavaClass();
        return Introspector.decapitalize(javaClass.getSimpleName());
    }


    /**
     * Set the ContextualLifecycle and the InjectionPoints for the AnnotatedType
     * @param type
     */
    protected void setDefaultBeanLifecycle(AnnotatedType<T> type)
    {
        InjectionTarget<T> injectionTarget;
        if (!type.getJavaClass().isInterface())
        {
            injectionTarget = beanManager.createInjectionTarget(type);
        }
        else
        {
            injectionTarget = new DummyInjectionTarget<T>();
        }
        this.beanLifecycle = new DelegatingContextualLifecycle<T>(injectionTarget);
        this.injectionPoints = injectionTarget.getInjectionPoints();
    }

    /**
     * <p>
     * Use the bean builder's current state to define the bean.
     * </p>
     *
     * @return the bean
     */
    public Bean<T> create()
    {
        if (!passivationCapable)
        {
            return new ImmutableBean<T>(beanClass, name, qualifiers, scope, stereotypes, types, alternative, nullable,
                    injectionPoints, toString, beanLifecycle);
        }
        else
        {
            return new ImmutablePassivationCapableBean<T>(beanClass, name, qualifiers, scope, stereotypes, types,
                    alternative, nullable, injectionPoints, toString, beanLifecycle, id);
        }
    }

    /**
     * Qualifiers currently defined for bean creation.
     *
     * @return the qualifiers current defined
     */
    public Set<Annotation> getQualifiers()
    {
        return qualifiers;
    }

    /**
     * Define the qualifiers used for bean creation.
     *
     * @param qualifiers the qualifiers to use
     */
    public BeanBuilder<T> qualifiers(Set<Annotation> qualifiers)
    {
        this.qualifiers = qualifiers;
        return this;
    }

    /**
     * Define the qualifiers used for bean creation.
     *
     * @param qualifiers the qualifiers to use
     */
    public BeanBuilder<T> qualifiers(Annotation... qualifiers)
    {
        this.qualifiers = new HashSet<Annotation>(Arrays.asList(qualifiers));
        return this;
    }

    /**
     * Add to the qualifiers used for bean creation.
     *
     * @param qualifier the additional qualifier to use
     */
    public BeanBuilder<T> addQualifier(Annotation qualifier)
    {
        this.qualifiers.add(qualifier);
        return this;
    }

    /**
     * Add to the qualifiers used for bean creation.
     *
     * @param qualifiers the additional qualifiers to use
     */
    public BeanBuilder<T> addQualifiers(Annotation... qualifiers)
    {
        this.qualifiers.addAll(new HashSet<Annotation>(Arrays.asList(qualifiers)));
        return this;
    }

    /**
     * Add to the qualifiers used for bean creation.
     *
     * @param qualifiers the additional qualifiers to use
     */
    public BeanBuilder<T> addQualifiers(Collection<Annotation> qualifiers)
    {
        this.qualifiers.addAll(qualifiers);
        return this;
    }

    /**
     * Scope currently defined for bean creation.
     *
     * @return the scope currently defined
     */
    public Class<? extends Annotation> getScope()
    {
        return scope;
    }

    /**
     * Define the scope used for bean creation.
     *
     * @param scope the scope to use
     */
    public BeanBuilder<T> scope(Class<? extends Annotation> scope)
    {
        this.scope = scope;
        return this;
    }

    /**
     * Stereotypes currently defined for bean creation.
     *
     * @return the stereotypes currently defined
     */
    public Set<Class<? extends Annotation>> getStereotypes()
    {
        return stereotypes;
    }

    /**
     * Define the stereotypes used for bean creation.
     *
     * @param stereotypes the stereotypes to use
     */
    public BeanBuilder<T> stereotypes(Set<Class<? extends Annotation>> stereotypes)
    {
        this.stereotypes = stereotypes;
        return this;
    }

    /**
     * Type closure currently defined for bean creation.
     *
     * @return the type closure currently defined
     */
    public Set<Type> getTypes()
    {
        return types;
    }

    /**
     * Define the type closure used for bean creation.
     *
     * @param types the type closure to use
     */
    public BeanBuilder<T> types(Set<Type> types)
    {
        this.types = types;
        return this;
    }

    /**
     * Define the type closure used for bean creation.
     *
     * @param types the type closure to use
     */
    public BeanBuilder<T> types(Type... types)
    {
        this.types = new HashSet<Type>(Arrays.asList(types));
        return this;
    }

    /**
     * Add to the type closure used for bean creation.
     *
     * @param type additional type to use
     */
    public BeanBuilder<T> addType(Type type)
    {
        this.types.add(type);
        return this;
    }

    /**
     * Add to the type closure used for bean creation.
     *
     * @param types the additional types to use
     */
    public BeanBuilder<T> addTypes(Type... types)
    {
        this.types.addAll(new HashSet<Type>(Arrays.asList(types)));
        return this;
    }

    /**
     * Add to the type closure used for bean creation.
     *
     * @param types the additional types to use
     */
    public BeanBuilder<T> addTypes(Collection<Type> types)
    {
        this.types.addAll(types);
        return this;
    }

    /**
     * Whether the created bean will be an alternative.
     *
     * @return <code>true</code> if the created bean will be an alternative,
     *         otherwise <code>false</code>
     */
    public boolean isAlternative()
    {
        return alternative;
    }

    /**
     * Define that the created bean will (or will not) be an alternative.
     *
     * @param alternative <code>true</code> if the created bean should be an
     *                    alternative, otherwise <code>false</code>
     */
    public BeanBuilder<T> alternative(boolean alternative)
    {
        this.alternative = alternative;
        return this;
    }

    /**
     * Whether the created bean will be nullable.
     *
     * @return <code>true</code> if the created bean will be nullable, otherwise
     *         <code>false</code>
     */
    public boolean isNullable()
    {
        return nullable;
    }

    /**
     * Define that the created bean will (or will not) be nullable.
     *
     * @param nullable <code>true</code> if the created bean should be nullable,
     *                 otherwise <code>false</code>
     */
    public BeanBuilder<T> nullable(boolean nullable)
    {
        this.nullable = nullable;
        return this;
    }

    /**
     * The {@link ContextualLifecycle} currently defined for bean creation.
     *
     * @return the bean lifecycle currently defined
     */
    public ContextualLifecycle<T> getBeanLifecycle()
    {
        return beanLifecycle;
    }

    /**
     * Define the {@link ContextualLifecycle} used for bean creation.
     *
     * @param beanLifecycle the {@link ContextualLifecycle} to use for bean
     *                      creation.
     */
    public BeanBuilder<T> beanLifecycle(ContextualLifecycle<T> beanLifecycle)
    {
        this.beanLifecycle = beanLifecycle;
        return this;
    }

    /**
     * The bean class currently defined for bean creation.
     *
     * @return the bean class currently defined.
     */
    public Class<?> getBeanClass()
    {
        return beanClass;
    }

    /**
     * Define the bean class used for bean creation.
     *
     * @param beanClass the bean class to use
     */
    public BeanBuilder<T> beanClass(Class<?> beanClass)
    {
        this.beanClass = beanClass;
        return this;
    }

    /**
     * The bean manager in use. This cannot be changed for this
     * {@link BeanBuilder}.
     *
     * @return the bean manager in use
     */
    public BeanManager getBeanManager()
    {
        return beanManager;
    }

    /**
     * The name of the bean currently defined for bean creation.
     *
     * @return the name of the bean or <code>null</code> if the bean has no name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Define the name of the bean used for bean creation.
     *
     * @param name the name of the bean to use or <code>null</code> if the bean
     *             should have no name
     */
    public BeanBuilder<T> name(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Whether the created bean will be passivation capable.
     *
     * @return <code>true</code> if the created bean will be passivation capable,
     *         otherwise <code>false</code>
     */
    public boolean isPassivationCapable()
    {
        return passivationCapable;
    }

    /**
     * Define that the created bean will (or will not) be passivation capable.
     *
     * @param passivationCapable <code>true</code> if the created bean should be
     *                           passivation capable, otherwise <code>false</code>
     */
    public BeanBuilder<T> passivationCapable(boolean passivationCapable)
    {
        this.passivationCapable = passivationCapable;
        return this;
    }

    /**
     * The id currently defined for bean creation.
     *
     * @return the id currently defined.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Define the id used for bean creation.
     *
     * @param id the id to use
     */
    public BeanBuilder<T> id(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * The injection points currently defined for bean creation.
     *
     * @return the injection points currently defined.
     */
    public Set<InjectionPoint> getInjectionPoints()
    {
        return injectionPoints;
    }

    /**
     * Define the injection points used for bean creation.
     *
     * @param injectionPoints the injection points to use
     */
    public BeanBuilder<T> injectionPoints(Set<InjectionPoint> injectionPoints)
    {
        this.injectionPoints = injectionPoints;
        return this;
    }

    /**
     * Define the string used when {@link #toString()} is called on the bean.
     *
     * @param toString the string to use
     */
    public BeanBuilder<T> toString(String toString)
    {
        this.toString = toString;
        return this;
    }

    /**
     * The string used when {@link #toString()} is called on the bean.
     *
     * @return the string currently defined
     */
    public String getToString()
    {
        return toString;
    }

}
