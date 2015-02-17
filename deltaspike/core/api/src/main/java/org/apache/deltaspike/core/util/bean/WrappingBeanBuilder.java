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
package org.apache.deltaspike.core.util.bean;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 * <p>A WrappingBeanBuilder helps creating Beans which internally
 * just wrap another existing Bean. The Types, Qualifiers
 * and other attributes of the resulting Bean can be modified.</p>
 * <p/>
 * <p>The {@link Bean#create(javax.enterprise.context.spi.CreationalContext)}
 * and {@link Bean#destroy(Object, javax.enterprise.context.spi.CreationalContext)}
 * methods will get delegated to the underlying wrapped Bean.</p>
 *
 * @param <T> the type of the Bean
 */
public class WrappingBeanBuilder<T> extends BeanBuilder<T>
{
    private final Bean<T> delegate;


    /**
     * Instantiate a new {@link WrappingBeanBuilder}.
     *
     * @param delegate    the delegate bean
     * @param beanManager current bean-manager
     */
    public WrappingBeanBuilder(Bean<Object> delegate, BeanManager beanManager)
    {
        super(beanManager);
        this.delegate = (Bean<T>) delegate;
    }

    protected void setDefaultBeanLifecycle(AnnotatedType<T> type)
    {
        // do nothing. We don't need that as we delegate this information
    }

    /**
     * <p>
     * Use the bean builder's current state to define the bean.
     * </p>
     *
     * @return the bean
     */
    public Bean<T> create()
    {
        if (isPassivationCapable())
        {
            return new ImmutablePassivationCapableBeanWrapper<T>(delegate,
                    name, qualifiers, scope, stereotypes, types, alternative,
                    nullable, toString, id);
        }
        else
        {
            return new ImmutableBeanWrapper<T>(delegate, name, qualifiers, scope,
                    stereotypes, types, alternative, nullable, toString);
        }
    }


}
