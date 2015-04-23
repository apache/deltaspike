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

package org.apache.deltaspike.core.impl.util;

import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;

import javax.enterprise.inject.Typed;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

@Typed()
public abstract class AnnotationInstanceUtils
{
    private AnnotationInstanceUtils()
    {
        // prevent instantiation
    }

    /**
     * @return a new instance of {@link javax.annotation.Priority} with the given value
     *         if the annotation-class is available in a cdi 1.1+ based environment, null otherwise
     */
    public static Annotation getPriorityAnnotationInstance(int priorityValue)
    {
        Annotation priorityAnnotationInstance = null;

        Class<? extends Annotation> priorityAnnotationClass =
            ClassUtils.tryToLoadClassForName("javax.annotation.Priority");

        //check for @Priority and CDI v1.1+
        if (priorityAnnotationClass != null &&
            ClassUtils.tryToLoadClassForName("javax.enterprise.inject.spi.AfterTypeDiscovery") != null)
        {
            Map<String, Object> defaultValueMap = new HashMap<String, Object>();
            defaultValueMap.put("value", priorityValue);
            priorityAnnotationInstance = AnnotationInstanceProvider.of(priorityAnnotationClass, defaultValueMap);
        }

        return priorityAnnotationInstance;
    }
}
