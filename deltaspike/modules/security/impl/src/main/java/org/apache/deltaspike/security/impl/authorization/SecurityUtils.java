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
package org.apache.deltaspike.security.impl.authorization;

import org.apache.deltaspike.security.api.authorization.annotation.SecurityBindingType;

import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.Typed;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

@Typed()
abstract class SecurityUtils
{
    private SecurityUtils()
    {
        // prevent instantiation
    }

    static boolean isMetaAnnotatedWithSecurityBindingType(Annotation annotation)
    {
        if (annotation.annotationType().isAnnotationPresent(SecurityBindingType.class))
        {
            return true;
        }

        List<Annotation> result = getAllAnnotations(annotation.annotationType().getAnnotations());

        for (Annotation foundAnnotation : result)
        {
            if (SecurityBindingType.class.isAssignableFrom(foundAnnotation.annotationType()))
            {
                return true;
            }
        }
        return false;
    }

    static Annotation resolveSecurityBindingType(Annotation annotation)
    {
        List<Annotation> result = getAllAnnotations(annotation.annotationType().getAnnotations());

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

    static List<Annotation> getAllAnnotations(Annotation[] annotations)
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

            result.add(annotation);
            result.addAll(getAllAnnotations(annotation.annotationType().getAnnotations()));
        }

        return result;
    }
}
