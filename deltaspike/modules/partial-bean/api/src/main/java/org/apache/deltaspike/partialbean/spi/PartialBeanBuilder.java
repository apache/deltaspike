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

public class PartialBeanBuilder
{
    private final Class<? extends Annotation> binding;
    private final Class<? extends InvocationHandler> handler;
    private final ArrayList<Class<?>> classes;
    
    public PartialBeanBuilder(Class<? extends Annotation> bindingClass, Class<? extends InvocationHandler> handlerClass)
    {
        this.binding = bindingClass;
        this.handler = handlerClass;
        
        this.classes = new ArrayList<Class<?>>();
    }
    
    public PartialBeanBuilder(Class<? extends Annotation> bindingClass, Class<? extends InvocationHandler> handlerClass,
            Class<?>... classes)
    {
        this.binding = bindingClass;
        this.handler = handlerClass;
        
        this.classes = new ArrayList<Class<?>>();
        this.classes.addAll(Arrays.asList(classes));
    }
    
    public PartialBeanBuilder addClass(Class<?> classToAdd)
    {
        classes.add(classToAdd);
        
        return this;
    }
    
    public PartialBeanBuilder addClasses(Class<?>... classesToAdd)
    {
        classes.addAll(Arrays.asList(classesToAdd));
        
        return this;
    }
    
    public PartialBeanDescriptor create()
    {
        return new PartialBeanDescriptor(binding, handler, classes);
    }
}
