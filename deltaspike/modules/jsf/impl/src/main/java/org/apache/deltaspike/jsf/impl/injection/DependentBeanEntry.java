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
package org.apache.deltaspike.jsf.impl.injection;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.io.Serializable;

class DependentBeanEntry<T> implements Serializable
{
    private static final long serialVersionUID = 7148484695430831322L;

    private final T instance;
    private final Bean<?> bean;
    private final CreationalContext<T> creationalContext;

    DependentBeanEntry(T instance, Bean<?> bean, CreationalContext<T> creationalContext)
    {
        this.instance = instance;
        this.bean = bean;
        this.creationalContext = creationalContext;
    }

    T getInstance()
    {
        return instance;
    }

    Bean<?> getBean()
    {
        return bean;
    }

    CreationalContext<T> getCreationalContext()
    {
        return creationalContext;
    }
}
