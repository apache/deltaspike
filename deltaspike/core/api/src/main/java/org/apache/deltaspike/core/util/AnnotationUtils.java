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

import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.Nonbinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

@Typed()
public abstract class AnnotationUtils
{
    private AnnotationUtils()
    {
        // prevent instantiation
    }

    public static <T extends Annotation> T extractAnnotationFromMethodOrClass(
        BeanManager beanManager, Method targetMethod, Class targetClass, Class<T> targetAnnotationType)
    {
        T result = extractAnnotationFromMethod(beanManager, targetMethod, targetAnnotationType);

        if (result == null)
        {
            //see DELTASPIKE-517
            Class unproxiedTargetClass = ProxyUtils.getUnproxiedClass(targetClass);

            // and if not found search on the class
            result = findAnnotation(beanManager, unproxiedTargetClass.getAnnotations(), targetAnnotationType);
        }
        return result;
    }

    public static <T extends Annotation> T extractAnnotationFromMethod(
        BeanManager beanManager, Method targetMethod, Class<T> targetAnnotationType)
    {
        return findAnnotation(beanManager, targetMethod.getAnnotations(), targetAnnotationType);
    }

    public static  <T extends Annotation> T findAnnotation(
            BeanManager beanManager, Annotation[] annotations, Class<T> targetAnnotationType)
    {
        for (Annotation annotation : annotations)
        {
            if (targetAnnotationType.equals(annotation.annotationType()))
            {
                return (T) annotation;
            }
            if (beanManager.isStereotype(annotation.annotationType()))
            {
                T result = findAnnotation(
                        beanManager, annotation.annotationType().getAnnotations(), targetAnnotationType);
                if (result != null)
                {
                    return result;
                }
            }
        }
        return null;
    }

    //based on org.apache.webbeans.container.BeanCacheKey#getQualifierHashCode
    public static int getQualifierHashCode(Annotation annotation)
    {
        Class annotationClass = annotation.annotationType();

        int hashCode = getTypeHashCode(annotationClass);

        for (Method member : annotationClass.getDeclaredMethods())
        {
            if (member.isAnnotationPresent(Nonbinding.class))
            {
                continue;
            }

            final Object annotationMemberValue = ReflectionUtils.invokeMethod(annotation, member, Object.class, true);

            final int arrayValue;
            if (annotationMemberValue == null /*possible with literals*/)
            {
                arrayValue = 0;
            }
            else if (annotationMemberValue.getClass().isArray())
            {
                Class<?> annotationMemberType = annotationMemberValue.getClass().getComponentType();
                if (annotationMemberType.isPrimitive())
                {
                    if (Long.TYPE == annotationMemberType)
                    {
                        arrayValue = Arrays.hashCode((long[]) annotationMemberValue);
                    }
                    else if (Integer.TYPE == annotationMemberType)
                    {
                        arrayValue = Arrays.hashCode((int[]) annotationMemberValue);
                    }
                    else if (Short.TYPE == annotationMemberType)
                    {
                        arrayValue = Arrays.hashCode((short[]) annotationMemberValue);
                    }
                    else if (Double.TYPE == annotationMemberType)
                    {
                        arrayValue = Arrays.hashCode((double[]) annotationMemberValue);
                    }
                    else if (Float.TYPE == annotationMemberType)
                    {
                        arrayValue = Arrays.hashCode((float[]) annotationMemberValue);
                    }
                    else if (Boolean.TYPE == annotationMemberType)
                    {
                        arrayValue = Arrays.hashCode((boolean[]) annotationMemberValue);
                    }
                    else if (Byte.TYPE == annotationMemberType)
                    {
                        arrayValue = Arrays.hashCode((byte[]) annotationMemberValue);
                    }
                    else if (Character.TYPE == annotationMemberType)
                    {
                        arrayValue = Arrays.hashCode((char[]) annotationMemberValue);
                    }
                    else
                    {
                        arrayValue = 0;
                    }
                }
                else
                {
                    arrayValue = Arrays.hashCode((Object[]) annotationMemberValue);
                }
            }
            else
            {
                arrayValue = annotationMemberValue.hashCode();
            }

            hashCode = 29 * hashCode + arrayValue;
            hashCode = 29 * hashCode + member.getName().hashCode();
        }

        return hashCode;
    }

    private static int getTypeHashCode(Type type)
    {
        int typeHash = type.hashCode();
        if (typeHash == 0 && type instanceof Class)
        {
            return ((Class)type).getName().hashCode();
        }

        return typeHash;
    }
}
