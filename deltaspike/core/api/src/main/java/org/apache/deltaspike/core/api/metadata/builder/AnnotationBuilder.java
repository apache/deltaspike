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

import org.apache.deltaspike.core.api.metadata.AnnotationStore;
import org.apache.deltaspike.core.api.util.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 */
//X TODO: JavaDoc the methods
public class AnnotationBuilder
{
    private final Map<Class<? extends Annotation>, Annotation> annotationMap;
    private final Set<Annotation> annotationSet;

    public AnnotationBuilder()
    {
        this.annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();
        this.annotationSet = new HashSet<Annotation>();
    }

    public AnnotationBuilder add(Annotation annotation)
    {
        if (annotation == null)
        {
            throw new IllegalArgumentException("annotation parameter must not be null");
        }
        annotationSet.add(annotation);
        annotationMap.put(annotation.annotationType(), annotation);
        return this;
    }

    public AnnotationBuilder remove(Class<? extends Annotation> annotationType)
    {
        if (annotationType == null)
        {
            throw new IllegalArgumentException("annotationType parameter must not be null");
        }

        Iterator<Annotation> it = annotationSet.iterator();
        while (it.hasNext())
        {
            Annotation an = it.next();
            if (annotationType.isAssignableFrom(an.annotationType()))
            {
                it.remove();
            }
        }
        annotationMap.remove(annotationType);
        return this;
    }

    public AnnotationStore create()
    {
        return new AnnotationStore(annotationMap, annotationSet);
    }

    public AnnotationBuilder addAll(Set<Annotation> annotations)
    {
        for (Annotation annotation : annotations)
        {
            add(annotation);
        }
        return this;
    }

    public AnnotationBuilder addAll(AnnotationStore annotations)
    {
        for (Annotation annotation : annotations.getAnnotations())
        {
            add(annotation);
        }
        return this;
    }

    public AnnotationBuilder addAll(AnnotatedElement element)
    {
        for (Annotation a : element.getAnnotations())
        {
            add(a);
        }
        return this;
    }

    public <T extends Annotation> T getAnnotation(Class<T> anType)
    {
        return Reflections.<T>cast(annotationMap.get(anType));
    }

    public boolean isAnnotationPresent(Class<?> annotationType)
    {
        return annotationMap.containsKey(annotationType);
    }

    @Override
    public String toString()
    {
        return annotationSet.toString();
    }
}
