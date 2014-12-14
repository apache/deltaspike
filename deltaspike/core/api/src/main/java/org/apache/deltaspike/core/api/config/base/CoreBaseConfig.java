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
package org.apache.deltaspike.core.api.config.base;

public interface CoreBaseConfig
{
    interface BeanManager
    {
        TypedConfig<Boolean> DELEGATE_LOOKUP =
            new TypedConfig<Boolean>("deltaspike.bean-manager.delegate_lookup", Boolean.TRUE);
    }

    interface Interceptor
    {
        TypedConfig<Integer> PRIORITY =
            new TypedConfig<Integer>("deltaspike.interceptor.priority", 0);
    }

    interface MBean
    {
        TypedConfig<Boolean> AUTO_UNREGISTER =
            new TypedConfig<Boolean>("deltaspike.mbean.auto-unregister", Boolean.TRUE);
    }

    interface Scope
    {
        interface Window
        {
            TypedConfig<Integer> MAX_COUNT =
                new TypedConfig<Integer>("deltaspike.scope.window.max-count", 1024);
        }
    }
}
