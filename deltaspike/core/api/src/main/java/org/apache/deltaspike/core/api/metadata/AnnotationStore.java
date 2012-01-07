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

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

/**
 * A helper class used to hold annotations on a type or member.
 */
//X TODO: JavaDoc
public class AnnotationStore
{
    private final Map<Class<? extends Annotation>, Annotation> annotationMap;
    private final Set<Annotation> annotationSet;

    public AnnotationStore(Map<Class<? extends Annotation>, Annotation> annotationMap, Set<Annotation> annotationSet)
    {
        this.annotationMap = annotationMap;
        this.annotationSet = unmodifiableSet(annotationSet);
    }

    public AnnotationStore()
    {
        this.annotationMap = emptyMap();
        this.annotationSet = emptySet();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType)
    {
        return annotationType.cast(annotationMap.get(annotationType));
    }

    public Set<Annotation> getAnnotations()
    {
        return annotationSet;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
    {
        return annotationMap.containsKey(annotationType);
    }
}
