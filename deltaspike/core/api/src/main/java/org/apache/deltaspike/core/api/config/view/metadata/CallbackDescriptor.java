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

import org.apache.deltaspike.core.api.config.view.metadata.annotation.DefaultCallback;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ExceptionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO bean-names
//TODO callback which allows to validate the method signature (during the descriptor creation)
public abstract class CallbackDescriptor<R>
{
    private List<CallbackEntry> callbacks = new ArrayList<CallbackEntry>();
    private Class<? extends Annotation> callbackType;

    protected CallbackDescriptor(Class beanClass, Class<? extends Annotation> callbackMarker)
    {
        init(new Class[]{beanClass}, callbackMarker);
    }

    protected CallbackDescriptor(Class[] beanClasses, Class<? extends Annotation> callbackMarker)
    {
        init(beanClasses, callbackMarker);
    }

    protected void init(Class[] targetBeanClasses, Class<? extends Annotation> callbackMarker)
    {
        if (callbackMarker == null)
        {
            callbackMarker = DefaultCallback.class;
        }

        this.callbackType = callbackMarker;

        //TODO discuss how deep we should scan
        for (Class targetBeanClass : targetBeanClasses)
        {
            CallbackEntry callbackEntry = new CallbackEntry(targetBeanClass, callbackMarker);
            if (!callbackEntry.callbackMethods.isEmpty())
            {
                this.callbacks.add(callbackEntry);
            }
        }
    }

    //TODO discuss if we should keep it here
    public List<R> execute(Object... optionalParams)
    {
        List<R> results = new ArrayList<R>();
        for (CallbackEntry callbackEntry : this.callbacks)
        {
            for (Method callbackMethod : callbackEntry.callbackMethods)
            {
                try
                {
                    Object bean = getTargetObject(callbackEntry.targetBeanClass);
                    R result = (R) callbackMethod.invoke(bean, optionalParams);

                    if (result != null)
                    {
                        results.add(result);
                    }
                }
                catch (Exception e)
                {
                    ExceptionUtils.throwAsRuntimeException(e);
                }
            }
        }
        return results;
    }

    public Map<Class, List<Method>> getCallbackMethods()
    {
        Map<Class, List<Method>> result = new HashMap<Class, List<Method>>(this.callbacks.size());

        for (CallbackEntry callbackEntry : this.callbacks)
        {
            result.put(callbackEntry.targetBeanClass, new ArrayList<Method>(callbackEntry.callbackMethods));
        }
        return result;
    }

    protected Object getTargetObject(Class targetType)
    {
        return BeanProvider.getContextualReference(targetType);
    }

    public boolean isBoundTo(Class<? extends Annotation> callbackType)
    {
        return this.callbackType.equals(callbackType);
    }

    private static class CallbackEntry
    {
        private List<Method> callbackMethods = new ArrayList<Method>();
        private final Class targetBeanClass;

        private CallbackEntry(Class beanClass, Class<? extends Annotation> callbackMarker)
        {
            this.targetBeanClass = beanClass;

            List<String> processedMethodNames = new ArrayList<String>();

            findMethodWithCallbackMarker(callbackMarker, beanClass, processedMethodNames);
        }

        private void findMethodWithCallbackMarker(Class<? extends Annotation> callbackMarker,
                                                  Class classToAnalyze,
                                                  List<String> processedMethodNames)
        {
            Class currentClass = classToAnalyze;

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
                for (Class interfaceClass : currentClass.getInterfaces())
                {
                    findMethodWithCallbackMarker(callbackMarker, interfaceClass, processedMethodNames);
                }

                currentClass = currentClass.getSuperclass();
            }
        }
    }
}
