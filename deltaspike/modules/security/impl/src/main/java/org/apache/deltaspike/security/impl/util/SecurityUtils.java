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
package org.apache.deltaspike.security.impl.util;

import org.apache.deltaspike.core.util.ReflectionUtils;
import org.apache.deltaspike.security.api.authorization.SecurityBindingType;
import org.apache.deltaspike.security.api.authorization.SecurityParameterBinding;

import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.Typed;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Typed()
public abstract class SecurityUtils
{
    private SecurityUtils()
    {
        // prevent instantiation
    }

    public static Set<Annotation> getSecurityBindingTypes(Class<?> targetClass, Method targetMethod)
    {
        Set<Annotation> securityBindingTypes = new HashSet<Annotation>();
        Class<?> cls = targetClass;
        while (cls != null && !cls.equals(Object.class))
        {
            for (final Annotation annotation : cls.getAnnotations())
            {
                if (SecurityUtils.isMetaAnnotatedWithSecurityBindingType(annotation))
                {
                    securityBindingTypes.add(annotation);
                }
            }
            cls = cls.getSuperclass();
        }

        for (final Annotation annotation : targetMethod.getAnnotations())
        {
            if (SecurityUtils.isMetaAnnotatedWithSecurityBindingType(annotation))
            {
                securityBindingTypes.add(annotation);
            }
        }
        return securityBindingTypes;
    }

    public static boolean isMetaAnnotatedWithSecurityBindingType(Annotation annotation)
    {
        if (annotation.annotationType().isAnnotationPresent(SecurityBindingType.class))
        {
            return true;
        }

        List<Annotation> result = getAllAnnotations(annotation.annotationType().getAnnotations(),
            new HashSet<Integer>());

        for (Annotation foundAnnotation : result)
        {
            if (SecurityBindingType.class.isAssignableFrom(foundAnnotation.annotationType()))
            {
                return true;
            }
        }
        return false;
    }

    public static Annotation resolveSecurityBindingType(Annotation annotation)
    {
        List<Annotation> result = getAllAnnotations(annotation.annotationType().getAnnotations(),
            new HashSet<Integer>());

        for (Annotation foundAnnotation : result)
        {
            if (foundAnnotation.annotationType().isAnnotationPresent(SecurityBindingType.class))
            {
                return foundAnnotation;
            }
        }
        throw new IllegalStateException(annotation.annotationType().getName() + " is a " + Stereotype.class.getName() +
                " but it isn't annotated with " + SecurityBindingType.class.getName());
    }

    public static boolean isMetaAnnotatedWithSecurityParameterBinding(Annotation annotation)
    {
        if (annotation.annotationType().isAnnotationPresent(SecurityParameterBinding.class))
        {
            return true;
        }

        List<Annotation> result = getAllAnnotations(annotation.annotationType().getAnnotations(),
            new HashSet<Integer>());

        for (Annotation foundAnnotation : result)
        {
            if (SecurityParameterBinding.class.isAssignableFrom(foundAnnotation.annotationType()))
            {
                return true;
            }
        }
        return false;
    }

    public static List<Annotation> getAllAnnotations(Annotation[] annotations, Set<Integer> annotationPath)
    {
        List<Annotation> result = new ArrayList<Annotation>();

        String annotationName;
        for (Annotation annotation : annotations)
        {
            annotationName = annotation.annotationType().getName();
            if (annotationName.startsWith("java.") || annotationName.startsWith("javax."))
            {
                continue;
            }

            int annotationHashCode = hashCodeOfAnnotation(annotation);
            if (!annotationPath.contains(annotationHashCode))
            {
                result.add(annotation);
                annotationPath.add(annotationHashCode);
                result.addAll(getAllAnnotations(annotation.annotationType().getAnnotations(), annotationPath));
            }
        }

        return result;
    }

    private static int hashCodeOfAnnotation(Annotation annotation)
    {
        //with using System#identityHashCode instead, we could detect the real instances
        //-> that would lead to multiple entries in the result which look the same (same type and members)

        //to detect real cycles, nonbinding members aren't ignored here
        return ReflectionUtils.calculateHashCodeOfAnnotation(annotation, false);
    }
}
