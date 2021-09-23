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

import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.AnnotatedType;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * An implementation of {@link AnnotatedMember} to be used in CDI life cycle events and
 * {@link org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder}.
 */
abstract class AnnotatedMemberImpl<X, M extends Member> extends AnnotatedImpl implements AnnotatedMember<X>
{
    private final AnnotatedType<X> declaringType;
    private final M javaMember;

    protected AnnotatedMemberImpl(AnnotatedType<X> declaringType, M member, Class<?> memberType,
                                  AnnotationStore annotations, Type genericType, Type overriddenType)
    {
        super(memberType, annotations, genericType, overriddenType);
        this.declaringType = declaringType;
        javaMember = member;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotatedType<X> getDeclaringType()
    {
        return declaringType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M getJavaMember()
    {
        return javaMember;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStatic()
    {
        return Modifier.isStatic(javaMember.getModifiers());
    }

}
