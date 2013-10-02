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
package org.apache.deltaspike.data.impl.util.bean;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

public class BeanDestroyable<T> implements Destroyable
{

    private final Bean<T> bean;
    private final T instance;
    private final CreationalContext<T> creationalContext;

    public BeanDestroyable(Bean<T> bean, T instance, CreationalContext<T> creationalContext)
    {
        this.bean = bean;
        this.instance = instance;
        this.creationalContext = creationalContext;
    }

    @Override
    public void destroy()
    {
        bean.destroy(instance, creationalContext);
    }

}
