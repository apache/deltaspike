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
package org.apache.deltaspike.partialbean.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PartialBeanDescriptor
{
    private Class<? extends Annotation> binding;
    private Class<? extends InvocationHandler> handler;
    private List<Class<?>> classes;

    public PartialBeanDescriptor(Class<? extends Annotation> binding)
    {
        this.binding = binding;
    }

    public PartialBeanDescriptor(Class<? extends Annotation> binding,
            Class<? extends InvocationHandler> handler)
    {
        this.binding = binding;
        this.handler = handler;
    }

    public PartialBeanDescriptor(Class<? extends Annotation> binding,
            Class<? extends InvocationHandler> handler,
            Class<?>... classes)
    {
        this.binding = binding;
        this.handler = handler;
        this.classes = new ArrayList<Class<?>>();
        if (classes.length > 0)
        {
            this.classes.addAll(Arrays.asList(classes));
        }
    }

    public PartialBeanDescriptor(Class<? extends Annotation> binding,
            Class<? extends InvocationHandler> handler,
            List<Class<?>> classes)
    {
        this.binding = binding;
        this.handler = handler;
        this.classes = classes;
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

    public List<Class<?>> getClasses()
    {
        return classes;
    }

    public void setClasses(List<Class<?>> classes)
    {
        this.classes = classes;
    }
}
