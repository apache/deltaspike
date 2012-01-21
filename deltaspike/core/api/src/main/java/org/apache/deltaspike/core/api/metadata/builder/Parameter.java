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

package org.apache.deltaspike.core.api.metadata.builder;

import org.apache.deltaspike.core.api.util.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;


/**
 * An implementation of Member for parameters
 */
abstract class Parameter<X> implements AnnotatedElement
{

    /**
     * Creates a new {@link Parameter} using the member and position passed in.
     *
     * @param declaringMember {@link Method} or {@link Constructor}.
     * @param position        position of the parameter
     * @param <X>             Type of the parameter
     * @return a new instance of Parameter
     */
    public static <X> Parameter<X> create(Member declaringMember, int position)
    {
        if (declaringMember instanceof Method)
        {
            return new MethodParameter<X>((Method) declaringMember, position);
        }
        else if (declaringMember instanceof Constructor<?>)
        {
            return new ConstructorParameter<X>(Reflections.<Constructor<X>>cast(declaringMember), position);
        }
        else
        {
            throw new IllegalArgumentException(
                    "Can only process members of type Method and Constructor, cannot process " + declaringMember);
        }
    }

    private static class MethodParameter<X> extends Parameter<X>
    {

        private final Method declaringMethod;

        private MethodParameter(Method declaringMethod, int position)
        {
            super(position);
            this.declaringMethod = declaringMethod;
        }

        @Override
        public Method getDeclaringMember()
        {
            return declaringMethod;
        }

        public Annotation[] getAnnotations()
        {
            if (declaringMethod.getParameterAnnotations().length > getPosition())
            {
                return declaringMethod.getParameterAnnotations()[getPosition()];
            }
            else
            {
                return Reflections.EMPTY_ANNOTATION_ARRAY;
            }
        }

        @Override
        public Type getBaseType()
        {
            if (declaringMethod.getGenericParameterTypes().length > getPosition())
            {
                return declaringMethod.getGenericParameterTypes()[getPosition()];
            }
            else
            {
                return declaringMethod.getParameterTypes()[getPosition()];
            }
        }

    }

    private static class ConstructorParameter<X> extends Parameter<X>
    {

        private final Constructor<X> declaringConstructor;

        private ConstructorParameter(Constructor<X> declaringConstructor, int position)
        {
            super(position);
            this.declaringConstructor = declaringConstructor;
        }

        @Override
        public Constructor<X> getDeclaringMember()
        {
            return declaringConstructor;
        }

        public Annotation[] getAnnotations()
        {
            if (declaringConstructor.getParameterAnnotations().length > getPosition())
            {
                return declaringConstructor.getParameterAnnotations()[getPosition()];
            }
            else
            {
                return Reflections.EMPTY_ANNOTATION_ARRAY;
            }
        }

        @Override
        public Type getBaseType()
        {
            if (declaringConstructor.getGenericParameterTypes().length > getPosition())
            {
                return declaringConstructor.getGenericParameterTypes()[getPosition()];
            }
            else
            {
                return declaringConstructor.getParameterTypes()[getPosition()];
            }
        }

    }

    private final int position;

    Parameter(int position)
    {
        this.position = position;
    }

    /**
     * Returns the actual {@link Member} declaring the parameter.
     *
     * @return Instance of the declaring java member.
     */
    public abstract Member getDeclaringMember();

    /**
     * Getter
     */
    public int getPosition()
    {
        return position;
    }

    @Override
    public int hashCode()
    {
        int hash = 1;
        hash = hash * 31 + getDeclaringMember().hashCode();
        hash = hash * 31 + Integer.valueOf(position).hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Parameter<?>)
        {
            Parameter<?> that = (Parameter<?>) obj;
            return this.getDeclaringMember().equals(that.getDeclaringMember()) &&
                    this.getPosition() == that.getPosition();
        }
        else
        {
            return false;
        }

    }

    /**
     * Returns the instance of the annotation on the member, or null if not found
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        for (Annotation annotation : getAnnotations())
        {
            if (annotation.annotationType().equals(annotationClass))
            {
                return annotationClass.cast(annotation);
            }
        }
        return null;
    }

    /**
     * Getter
     */
    public Annotation[] getDeclaredAnnotations()
    {
        return getAnnotations();
    }

    /**
     * Simple test if an annotation is present.
     */
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass)
    {
        return getAnnotation(annotationClass) != null;
    }

    /**
     * Getter
     */
    public abstract Type getBaseType();

}
