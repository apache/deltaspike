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

package org.apache.deltaspike.core.api.metadata;

import org.apache.deltaspike.core.api.metadata.builder.AnnotationBuilder;
import org.apache.deltaspike.core.api.util.Reflections;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

/**
 * Provides access to the context of an annotation redefinition.
 *
 * @see org.apache.deltaspike.core.api.metadata.builder.AnnotatedTypeBuilder
 * @see AnnotationRedefiner
 */
public class RedefinitionContext
{

    private final AnnotatedElement annotatedElement;
    private final Type baseType;
    private final AnnotationBuilder annotationBuilder;
    private final String elementName;

    public RedefinitionContext(AnnotatedElement annotatedElement, Type baseType, AnnotationBuilder annotationBuilder, String elementName)
    {
        this.annotatedElement = annotatedElement;
        this.baseType = baseType;
        this.annotationBuilder = annotationBuilder;
        this.elementName = elementName;
    }

    /**
     * Access to the {@link AnnotatedElement} on which this annotation is
     * defined. If the annotation is defined on a Field, this may be cast to
     * {@link java.lang.reflect.Field}, if defined on a method, this may be cast to {@link java.lang.reflect.Method},
     * if defined on a constructor, this may be cast to {@link java.lang.reflect.Constructor}, if
     * defined on a class, this may be cast to {@link Class}, or if
     * defined on a parameter, this may be cast to {@link org.apache.deltaspike.core.api.metadata.builder.Parameter}
     */
    public AnnotatedElement getAnnotatedElement()
    {
        return annotatedElement;
    }

    /**
     * Access to the {@link Type} of the element on which this annotation is
     * defined
     */
    public Type getBaseType()
    {
        return baseType;
    }

    /**
     * Access to the raw type of the element on which the annotation is defined
     *
     * @return
     */
    public Class<?> getRawType()
    {
        return Reflections.getRawType(baseType);
    }

    /**
     * Access to the annotations present on the element. It is safe to modify the
     * annotations present using the {@link AnnotationBuilder}
     */
    public AnnotationBuilder getAnnotationBuilder()
    {
        return annotationBuilder;
    }

    /**
     * Access to the name of the element, or null if this represents a
     * constructor, parameter or class.
     */
    public String getElementName()
    {
        return elementName;
    }

}
