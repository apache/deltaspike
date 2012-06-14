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
package org.apache.deltaspike.core.util;

import org.apache.deltaspike.core.util.metadata.builder.ImmutableInjectionPoint;

import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A set of utility methods for working with beans.
 */
@Typed()
public abstract class BeanUtils
{

    private BeanUtils()
    {
        // prevent instantiation
    }

    /**
     * Extract the qualifiers from a set of annotations.
     *
     * @param beanManager the beanManager to use to determine if an annotation is
     *                    a qualifier
     * @param annotations the annotations to check
     * @return any qualifiers present in <code>annotations</code>
     */
    @SuppressWarnings("unchecked")
    public static Set<Annotation> getQualifiers(BeanManager beanManager, Iterable<Annotation> annotations)
    {
        Set<Annotation> qualifiers = new HashSet<Annotation>();

        for (Annotation annotation : annotations)
        {
            if (beanManager.isQualifier(annotation.annotationType()))
            {
                qualifiers.add(annotation);
            }
        }

        return qualifiers;
    }

    /**
     * Extract the qualifiers from a set of annotations.
     *
     * @param beanManager the beanManager to use to determine if an annotation is
     *                    a qualifier
     * @param annotations the annotations to check
     * @return any qualifiers present in <code>annotations</code>
     */
    public static Set<Annotation> getQualifiers(BeanManager beanManager, Annotation[]... annotations)
    {
        Set<Annotation> qualifiers = new HashSet<Annotation>();
        for (Annotation[] annotationArray : annotations)
        {
            for (Annotation annotation : annotationArray)
            {
                if (beanManager.isQualifier(annotation.annotationType()))
                {
                    qualifiers.add(annotation);
                }
            }
        }
        return qualifiers;
    }

    /**
     * @param annotated element to search in
     * @param targetType target type to search for
     * @param <T> type of the Annotation which get searched
     * @return annotation instance extracted from the annotated member
     */
    public static <T extends Annotation> T extractAnnotation(Annotated annotated, Class<T> targetType)
    {
        T result = annotated.getAnnotation(targetType);

        if (result == null)
        {
            for (Annotation annotation : annotated.getAnnotations())
            {
                result = annotation.annotationType().getAnnotation(targetType);

                if (result != null)
                {
                    break;
                }
            }
        }

        return result;
    }


    /**
     * Given a method, and the bean on which the method is declared, create a
     * collection of injection points representing the parameters of the method.
     *
     * @param <X>           the type declaring the method
     * @param method        the method
     * @param declaringBean the bean on which the method is declared
     * @param beanManager   the bean manager to use to create the injection points
     * @return the injection points
     */
    public static <X> List<InjectionPoint> createInjectionPoints(AnnotatedMethod<X> method, Bean<?> declaringBean,
                                                                 BeanManager beanManager)
    {
        List<InjectionPoint> injectionPoints = new ArrayList<InjectionPoint>();
        for (AnnotatedParameter<X> parameter : method.getParameters())
        {
            InjectionPoint injectionPoint =
                    new ImmutableInjectionPoint(parameter, beanManager, declaringBean, false, false);

            injectionPoints.add(injectionPoint);
        }
        return injectionPoints;
    }
}
