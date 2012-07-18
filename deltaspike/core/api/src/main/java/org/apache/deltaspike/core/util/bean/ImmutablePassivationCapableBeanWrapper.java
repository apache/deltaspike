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

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.PassivationCapable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * <p>PassivationCapable version of an ImmutableBeanWrapper.
 * You can easily create instances of this class with the
 * {@link WrappingBeanBuilder}.</p>
 *
 * @see ImmutableBeanWrapper
 * @see WrappingBeanBuilder
 */
public class ImmutablePassivationCapableBeanWrapper<T> extends ImmutableBeanWrapper<T>
        implements PassivationCapable
{
    private final String id;

    /**
     * Instantiate a new {@link ImmutableBeanWrapper} for a  {@link PassivationCapable} Bean.
     *
     * @param bean        the bean to wrapped the lifecycle to
     * @param name        the name of the bean
     * @param qualifiers  the qualifiers of the bean
     * @param scope       the scope of the bean
     * @param stereotypes the bean's stereotypes
     * @param types       the types of the bean
     * @param alternative whether the bean is an alternative
     * @param nullable    true if the bean is nullable
     * @param toString    the string which should be returned by #{@link #toString()}
     * @param id          the passivationId which gets returned by {@link #getId()}
     */
    public ImmutablePassivationCapableBeanWrapper(Bean<T> bean,
                                                  String name,
                                                  Set<Annotation> qualifiers,
                                                  Class<? extends Annotation> scope,
                                                  Set<Class<? extends Annotation>> stereotypes,
                                                  Set<Type> types,
                                                  boolean alternative,
                                                  boolean nullable,
                                                  String toString,
                                                  String id)
    {
        super(bean, name, qualifiers, scope, stereotypes, types, alternative, nullable, toString);
        this.id = id;
    }

    @Override
    public String getId()
    {
        return id;
    }
}
