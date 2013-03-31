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
package org.apache.deltaspike.jsf.impl.config.view;

import org.apache.deltaspike.core.api.config.view.metadata.ExecutableCallbackDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.DefaultCallback;
import org.apache.deltaspike.core.api.config.view.metadata.CallbackDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ConfigDescriptor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

abstract class AbstractPathConfigDescriptor<CT> implements ConfigDescriptor<CT>
{
    private final Class<? extends CT> source;
    private List<Annotation> metaData;
    private Map<Class<? extends Annotation>, List<CallbackDescriptor>> callbackDescriptors;

    AbstractPathConfigDescriptor(Class<? extends CT> configClass,
                                 List<Annotation> mergedMetaData,
                                 Map<Class<? extends Annotation>, List<CallbackDescriptor>> callbackDescriptors)
    {
        this.source = configClass;
        this.metaData = Collections.unmodifiableList(mergedMetaData);
        this.callbackDescriptors = callbackDescriptors;
    }

    @Override
    public Class<? extends CT> getConfigClass()
    {
        return this.source;
    }

    @Override
    public List<Annotation> getMetaData()
    {
        return this.metaData;
    }

    @Override
    public <T extends Annotation> List<T> getMetaData(Class<T> target)
    {
        List<T> result = new ArrayList<T>();

        for (Annotation annotation : this.metaData)
        {
            if (target.isAssignableFrom(annotation.annotationType()))
            {
                result.add((T) annotation);
            }
        }
        return result;
    }

    @Override
    public CallbackDescriptor getCallbackDescriptor(Class<? extends Annotation> metaDataType)
    {
        return getCallbackDescriptor(metaDataType, DefaultCallback.class);
    }

    @Override
    public CallbackDescriptor getCallbackDescriptor(Class<? extends Annotation> metaDataType,
                                                    Class<? extends Annotation> callbackType)
    {
        return findCallbackDescriptor(metaDataType, callbackType);
    }

    @Override
    public <T extends ExecutableCallbackDescriptor> T getExecutableCallbackDescriptor(
            Class<? extends Annotation> metaDataType,
            Class<? extends T> executorType)
    {
        return getExecutableCallbackDescriptor(metaDataType, DefaultCallback.class, executorType);
    }

    @Override
    public <T extends ExecutableCallbackDescriptor> T getExecutableCallbackDescriptor(
            Class<? extends Annotation> metaDataType,
            Class<? extends Annotation> callbackType,
            Class<? extends T> executorType)
    {
        return findCallbackDescriptor(metaDataType, callbackType);
    }

    private <T extends CallbackDescriptor> T findCallbackDescriptor(Class<? extends Annotation> metaDataType,
                                                                    Class<? extends Annotation> callbackType)
    {
        List<CallbackDescriptor> foundDescriptors = callbackDescriptors.get(metaDataType);

        if (foundDescriptors == null || foundDescriptors.isEmpty())
        {
            return null;
        }

        if (callbackType == null || DefaultCallback.class.equals(callbackType))
        {
            if (foundDescriptors.size() > 1)
            {
                //TODO validate during bootstrapping
                throw new IllegalStateException("multiple descriptors for " +
                        ((callbackType == null) ? DefaultCallback.class.getName() : callbackType.getName()) +
                        " aren't allowed");
            }
            return (T)foundDescriptors.iterator().next();
        }

        for (CallbackDescriptor callbackDescriptor : foundDescriptors)
        {
            if (callbackDescriptor.isBoundTo(callbackType))
            {
                return (T)callbackDescriptor;
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        return getPath();
    }
}
