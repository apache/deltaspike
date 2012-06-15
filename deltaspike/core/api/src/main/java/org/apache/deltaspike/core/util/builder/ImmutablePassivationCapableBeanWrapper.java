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
package org.apache.deltaspike.core.util.builder;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.PassivationCapable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * PassivationCapable version of an ImmutableBeanWrapper.
 *
 * @see ImmutableBeanWrapper
 */
public class ImmutablePassivationCapableBeanWrapper<T> extends ImmutableBeanWrapper<T>
        implements PassivationCapable
{
    private String id;

    public ImmutablePassivationCapableBeanWrapper(Bean<T> delegate,
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
        super(delegate, name, qualifiers, scope, stereotypes, types, alternative, nullable, toString);
        this.id = id;
    }

    @Override
    public String getId()
    {
        return id;
    }
}
