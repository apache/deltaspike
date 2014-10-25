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

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * {@link ExecutableCallbackDescriptor} for simple callback methods without (supported) parameters, which exposes
 * #execute without parameters.
 *
 * @param <R> return type
 */
public abstract class SimpleCallbackDescriptor<R> extends ExecutableCallbackDescriptor<R>
{
    protected SimpleCallbackDescriptor(Class<?> beanClass, Class<? extends Annotation> callbackMarker)
    {
        super(beanClass, callbackMarker);
    }

    protected SimpleCallbackDescriptor(Class<?>[] beanClasses, Class<? extends Annotation> callbackMarker)
    {
        super(beanClasses, callbackMarker);
    }

    public List<R> execute()
    {
        return super.execute();
    }
}
