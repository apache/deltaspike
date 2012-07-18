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
import org.apache.deltaspike.core.util.ArraysUtils;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>A WrappingBeanBuilder helps creating Beans which internally
 * just wrap another existing Bean. The Types, Qualifiers
 * and other attributes of the resulting Bean can be modified.</p>
 * <p/>
 * <p>The {@link Bean#create(javax.enterprise.context.spi.CreationalContext)}
 * and {@link Bean#destroy(Object, javax.enterprise.context.spi.CreationalContext)}
 * methods will get delegated to the underlying wrapped Bean.</p>
 *
 * @param <T> the type of the Bean
 */
public class WrappingBeanBuilder<T>
{
    private final Bean<T> delegate;
    private final BeanManager beanManager;

    private Set<Type> types;
    private Set<Annotation> qualifiers;
    private String name;
    private Class<? extends Annotation> scope;
    private boolean alternative;
    private boolean nullable;
    private String toString;
    private Set<Class<? extends Annotation>> stereotypes;
    private boolean passivationCapable;
    private String id;

    /**
     * Instantiate a new {@link WrappingBeanBuilder}.
     *
     * @param delegate    the delegate bean
     * @param beanManager current bean-manager
     */
    public WrappingBeanBuilder(Bean<Object> delegate, BeanManager beanManager)
    {
        this.delegate = (Bean<T>) delegate;
        this.beanManager = beanManager;
    }

    /**
     * <p>
     * Read the {@link AnnotatedType}, creating a narrowing bean from the class
     * and its annotations.
     * </p>
     *
     * @param type the type to read
     */
    public WrappingBeanBuilder<T> readFromType(AnnotatedType<T> type)
    {
        types = new HashSet<Type>(type.getTypeClosure());
        qualifiers = new HashSet<Annotation>();
        stereotypes = new HashSet<Class<? extends Annotation>>();
        String name = null;
        Class<? extends Annotation> scope = Dependent.class;

        for (Annotation annotation : type.getAnnotations())
        {
            if (annotation.annotationType().equals(Named.class))
            {
                // it's important to scan for Named first
                // as it's also a qualifier!
                name = Named.class.cast(annotation).value();
            }
            else if (beanManager.isQualifier(annotation.annotationType()))
            {
                qualifiers.add(annotation);
            }
            else if (beanManager.isScope(annotation.annotationType()))
            {
                scope = annotation.annotationType();
            }
            else if (beanManager.isStereotype(annotation.annotationType()))
            {
                stereotypes.add(annotation.annotationType());
            }
        }
        if (qualifiers.isEmpty())
        {
            qualifiers.add(new DefaultLiteral());
        }
        qualifiers.add(new AnyLiteral());
        this.name = "".equals(name) ? null : name;
        this.scope = scope;
        alternative = type.isAnnotationPresent(Alternative.class);
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
    public WrappingBeanBuilder<T> types(Set<Type> types)
    {
        this.types = types;
        return this;
    }

    /**
     * Define the type closure used for bean creation.
     *
     * @param types the type closure to use
     */
    public WrappingBeanBuilder<T> types(Type... types)
    {
        this.types = ArraysUtils.asSet(types);
        return this;
    }

    /**
     * Add to the type closure used for bean creation.
     *
     * @param type additional type to use
     */
    public WrappingBeanBuilder<T> addType(Type type)
    {
        types.add(type);
        return this;
    }

    /**
     * Add to the type closure used for bean creation.
     *
     * @param types the additional types to use
     */
    public WrappingBeanBuilder<T> addTypes(Type... types)
    {
        this.types.addAll(ArraysUtils.asSet(types));
        return this;
    }

    /**
     * Add to the type closure used for bean creation.
     *
     * @param types the additional types to use
     */
    public WrappingBeanBuilder<T> addTypes(Collection<Type> types)
    {
        this.types.addAll(types);
        return this;
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
    public WrappingBeanBuilder<T> qualifiers(Set<Annotation> qualifiers)
    {
        this.qualifiers = qualifiers;
        return this;
    }

    /**
     * Define the qualifiers used for bean creation.
     *
     * @param qualifiers the qualifiers to use
     */
    public WrappingBeanBuilder<T> qualifiers(Annotation... qualifiers)
    {
        this.qualifiers = ArraysUtils.asSet(qualifiers);
        return this;
    }

    /**
     * Add to the qualifiers used for bean creation.
     *
     * @param qualifier the additional qualifier to use
     */
    public WrappingBeanBuilder<T> addQualifier(Annotation qualifier)
    {
        qualifiers.add(qualifier);
        return this;
    }

    /**
     * Add to the qualifiers used for bean creation.
     *
     * @param qualifiers the additional qualifiers to use
     */
    public WrappingBeanBuilder<T> addQualifiers(Annotation... qualifiers)
    {
        this.qualifiers.addAll(ArraysUtils.asSet(qualifiers));
        return this;
    }

    /**
     * Add to the qualifiers used for bean creation.
     *
     * @param qualifiers the additional qualifiers to use
     */
    public WrappingBeanBuilder<T> addQualifiers(
            Collection<Annotation> qualifiers)
    {
        this.qualifiers.addAll(qualifiers);
        return this;
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
    public WrappingBeanBuilder<T> name(String name)
    {
        this.name = name;
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
    public WrappingBeanBuilder<T> scope(Class<? extends Annotation> scope)
    {
        this.scope = scope;
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
    public WrappingBeanBuilder<T> alternative(boolean alternative)
    {
        this.alternative = alternative;
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
    public WrappingBeanBuilder<T> stereotypes(
            Set<Class<? extends Annotation>> stereotypes)
    {
        this.stereotypes = stereotypes;
        return this;
    }

    /**
     * <p>
     * Use the bean builder's current state to define the bean.
     * </p>
     *
     * @return the bean
     */
    public ImmutableBeanWrapper<T> create()
    {
        if (isPassivationCapable())
        {
            return new ImmutablePassivationCapableBeanWrapper<T>(delegate,
                    name, qualifiers, scope, stereotypes, types, alternative,
                    nullable, toString, id);
        }
        else
        {
            return new ImmutableBeanWrapper<T>(delegate, name, qualifiers, scope,
                    stereotypes, types, alternative, nullable, toString);
        }
    }

    // }

    /**
     * The string used when {@link #toString()} is called on the bean.
     *
     * @return the string currently defined
     */
    public String getToString()
    {
        return toString;
    }

    /**
     * Define the string used when {@link #toString()} is called on the bean.
     *
     * @param toString the string to use
     */
    public WrappingBeanBuilder<T> toString(String toString)
    {
        this.toString = toString;
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
    public WrappingBeanBuilder<T> nullable(boolean nullable)
    {
        this.nullable = nullable;
        return this;
    }

    /**
     * Whether the created bean will be passivation capable.
     *
     * @return <code>true</code> if the created bean will be passivation
     *         capable, otherwise <code>false</code>
     */
    public boolean isPassivationCapable()
    {
        return passivationCapable;
    }

    /**
     * Define that the created bean will (or will not) be passivation capable.
     *
     * @param passivationCapable <code>true</code> if the created bean should be passivation
     *                           capable, otherwise <code>false</code>
     */
    public WrappingBeanBuilder<T> passivationCapable(boolean passivationCapable)
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
    public WrappingBeanBuilder<T> id(String id)
    {
        this.id = id;
        return this;
    }
}
