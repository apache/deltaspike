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

import org.apache.deltaspike.core.util.HierarchyDiscovery;

import jakarta.enterprise.inject.spi.Annotated;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The base class for all New Annotated types.
 */
abstract class AnnotatedImpl implements Annotated
{

    private final Type type;
    private final Set<Type> typeClosure;
    private final AnnotationStore annotations;

    protected AnnotatedImpl(Class<?> type, AnnotationStore annotations, Type genericType, Type overriddenType)
    {
        if (overriddenType == null)
        {
            if (genericType != null)
            {
                typeClosure = new HierarchyDiscovery(genericType).getTypeClosure();
                this.type = genericType;
            }
            else
            {
                typeClosure = new HierarchyDiscovery(type).getTypeClosure();
                this.type = type;
            }
        }
        else
        {
            this.type = overriddenType;
            typeClosure = Collections.singleton(overriddenType);
        }


        if (annotations == null)
        {
            this.annotations = new AnnotationStore();
        }
        else
        {
            this.annotations = annotations;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType)
    {
        return annotations.getAnnotation(annotationType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Annotation> getAnnotations()
    {
        return annotations.getAnnotations();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
    {
        return annotations.isAnnotationPresent(annotationType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Type> getTypeClosure()
    {
        return new HashSet<Type>(typeClosure);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getBaseType()
    {
        return type;
    }

}
