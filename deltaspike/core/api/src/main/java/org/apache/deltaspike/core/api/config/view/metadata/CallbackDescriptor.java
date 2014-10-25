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
package org.apache.deltaspike.core.api.config.view.metadata;

import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.inject.Named;
import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic descriptor for a given class and callback type. It finds and caches the method(s) of the given class which are
 * annotated with the given callback-type.
 */
public abstract class CallbackDescriptor
{
    protected List<CallbackEntry> callbacks = new ArrayList<CallbackEntry>();
    protected Class<? extends Annotation> callbackType;

    protected CallbackDescriptor(Class<?> beanClass, Class<? extends Annotation> callbackMarker)
    {
        init(new Class[]{beanClass}, callbackMarker);
    }

    protected CallbackDescriptor(Class<?>[] beanClasses, Class<? extends Annotation> callbackMarker)
    {
        init(beanClasses, callbackMarker);
    }

    protected void init(Class<?>[] targetBeanClasses, Class<? extends Annotation> callbackMarker)
    {
        if (callbackMarker == null)
        {
            callbackMarker = DefaultCallback.class;
        }

        this.callbackType = callbackMarker;

        //TODO discuss how deep we should scan
        for (Class<?> targetBeanClass : targetBeanClasses)
        {
            CallbackEntry callbackEntry = new CallbackEntry(targetBeanClass, callbackMarker);
            if (!callbackEntry.callbackMethods.isEmpty())
            {
                this.callbacks.add(callbackEntry);
            }
        }
    }

    public Map<Class<?>, List<Method>> getCallbackMethods()
    {
        Map<Class<?>, List<Method>> result = new HashMap<Class<?>, List<Method>>(this.callbacks.size());

        for (CallbackEntry callbackEntry : this.callbacks)
        {
            result.put(callbackEntry.targetBeanClass, new ArrayList<Method>(callbackEntry.callbackMethods));
        }
        return result;
    }

    protected <T> T getTargetObject(Class<T> targetType)
    {
        return BeanProvider.getContextualReference(targetType, true);
    }

    protected Object getTargetObjectByName(String beanName)
    {
        return BeanProvider.getContextualReference(beanName, true);
    }

    public boolean isBoundTo(Class<? extends Annotation> callbackType)
    {
        return this.callbackType.equals(callbackType);
    }

    protected static class CallbackEntry
    {
        private List<Method> callbackMethods = new ArrayList<Method>();
        private final Class<?> targetBeanClass;
        private final String beanName;

        private CallbackEntry(Class<?> beanClass, Class<? extends Annotation> callbackMarker)
        {
            this.targetBeanClass = beanClass;

            Named named = this.targetBeanClass.getAnnotation(Named.class);

            if (named != null && !"".equals(named.value()))
            {
                this.beanName = named.value();
            }
            else
            {
                //fallback to the default (which might exist) -> TODO check meta-data of Bean<T>
                this.beanName = Introspector.decapitalize(targetBeanClass.getSimpleName());
            }

            List<String> processedMethodNames = new ArrayList<String>();

            findMethodWithCallbackMarker(callbackMarker, beanClass, processedMethodNames);
        }

        private void findMethodWithCallbackMarker(Class<? extends Annotation> callbackMarker,
                                                  Class<?> classToAnalyze,
                                                  List<String> processedMethodNames)
        {
            Class<?> currentClass = classToAnalyze;

            while (currentClass != null && !Object.class.getName().equals(currentClass.getName()))
            {
                for (Method currentMethod : currentClass.getDeclaredMethods())
                {
                    //don't process overridden methods
                    //ds now allows callbacks with parameters -> TODO refactor this approach
                    if (processedMethodNames.contains(currentMethod.getName()))
                    {
                        continue;
                    }

                    if (currentMethod.isAnnotationPresent(callbackMarker))
                    {
                        processedMethodNames.add(currentMethod.getName());

                        if (Modifier.isPrivate(currentMethod.getModifiers()))
                        {
                            throw new IllegalStateException(
                                "Private methods aren't supported to avoid side-effects with normal-scoped CDI beans." +
                                    " Please use e.g. protected or public instead. ");
                        }

                        currentMethod.setAccessible(true);
                        this.callbackMethods.add(currentMethod);
                    }
                }

                //scan interfaces
                for (Class<?> interfaceClass : currentClass.getInterfaces())
                {
                    findMethodWithCallbackMarker(callbackMarker, interfaceClass, processedMethodNames);
                }

                currentClass = currentClass.getSuperclass();
            }
        }

        public List<Method> getCallbackMethods()
        {
            return callbackMethods;
        }

        public Class<?> getTargetBeanClass()
        {
            return targetBeanClass;
        }

        public String getBeanName()
        {
            return beanName;
        }
    }
}
