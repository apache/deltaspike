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

import org.apache.deltaspike.core.util.ExceptionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Specialized {@link CallbackDescriptor} which provides {@link #execute} only for concrete descriptors, but doesn't
 * expose it (and can't get used by accident). Concrete implementations can provide type-safe versions of it, but
 * delegate the final execution to {@link #execute}.
 *
 * @param <R> return type
 */
public abstract class ExecutableCallbackDescriptor<R> extends CallbackDescriptor
{
    protected ExecutableCallbackDescriptor(Class<?> beanClass, Class<? extends Annotation> callbackMarker)
    {
        super(beanClass, callbackMarker);
    }

    protected ExecutableCallbackDescriptor(Class<?>[] beanClasses, Class<? extends Annotation> callbackMarker)
    {
        super(beanClasses, callbackMarker);
    }

    protected List<R> execute(Object... parameters)
    {
        List<R> results = new ArrayList<R>();
        for (CallbackEntry callbackEntry : this.callbacks)
        {
            for (Method callbackMethod : callbackEntry.getCallbackMethods())
            {
                try
                {
                    Class<?> targetBeanClass = callbackEntry.getTargetBeanClass();
                    Object bean = getTargetObject(targetBeanClass);

                    if (bean == null)
                    {
                        String beanName = callbackEntry.getBeanName();
                        bean = getTargetObjectByName(beanName);

                        if (bean == null)
                        {
                            throw new IllegalStateException("Can't find bean by type " + targetBeanClass +
                                " or by name: " + beanName);
                        }
                    }

                    @SuppressWarnings("unchecked")
                    R result = (R) callbackMethod.invoke(bean, parameters);

                    if (result != null)
                    {
                        results.add(result);
                    }
                }
                catch (Exception e)
                {
                    if (e instanceof InvocationTargetException)
                    {
                        ExceptionUtils.throwAsRuntimeException(e.getCause());
                    }
                    ExceptionUtils.throwAsRuntimeException(e);
                }
            }
        }
        return results;
    }
}
