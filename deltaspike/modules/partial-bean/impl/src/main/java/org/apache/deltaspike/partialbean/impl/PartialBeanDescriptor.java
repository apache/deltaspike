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
package org.apache.deltaspike.partialbean.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.util.HashSet;
import java.util.Set;

public class PartialBeanDescriptor
{
    private Class<? extends Annotation> binding;
    private Class<? extends InvocationHandler> handler;
    private Set<Class<?>> classes;

    public PartialBeanDescriptor(Class<? extends Annotation> binding)
    {
        this.binding = binding;
        this.classes = new HashSet<>();
    }

    public PartialBeanDescriptor(Class<? extends Annotation> binding,
            Class<? extends InvocationHandler> handler)
    {
        this(binding);
        this.handler = handler;
    }

    public PartialBeanDescriptor(Class<? extends Annotation> binding,
            Class<? extends InvocationHandler> handler,
            Class<?> clazz)
    {
        this(binding, handler);
        this.classes.add(clazz);
    }
    
    public Class<? extends Annotation> getBinding()
    {
        return binding;
    }

    public void setBinding(Class<? extends Annotation> binding)
    {
        this.binding = binding;
    }

    public Class<? extends InvocationHandler> getHandler()
    {
        return handler;
    }

    public void setHandler(Class<? extends InvocationHandler> handler)
    {
        this.handler = handler;
    }

    public Set<Class<?>> getClasses()
    {
        return classes;
    }

    public void setClasses(Set<Class<?>> classes)
    {
        this.classes = classes;
    }
}
