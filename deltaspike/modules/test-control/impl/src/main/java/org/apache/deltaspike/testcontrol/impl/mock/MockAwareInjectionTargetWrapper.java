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
package org.apache.deltaspike.testcontrol.impl.mock;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.testcontrol.api.mock.DynamicMockManager;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

public class MockAwareInjectionTargetWrapper<T> implements InjectionTarget<T>
{
    private BeanManager beanManager;
    private final InjectionTarget<T> wrapped;
    private final List<Type> beanTypes;
    private final List<Annotation> qualifiers;

    public MockAwareInjectionTargetWrapper(BeanManager beanManager,
                                           InjectionTarget<T> wrapped,
                                           List<Type> beanTypes,
                                           List<Annotation> qualifiers)
    {
        this.beanManager = beanManager;
        this.wrapped = wrapped;
        this.beanTypes = beanTypes;
        this.qualifiers = qualifiers;
    }

    @Override
    public T produce(CreationalContext<T> creationalContext)
    {
        DynamicMockManager mockManager =
            BeanProvider.getContextualReference(this.beanManager, DynamicMockManager.class, false);

        for (Type beanType : this.beanTypes)
        {
            Object mockInstance = mockManager.getMock(
                    (Class)beanType, this.qualifiers.toArray(new Annotation[this.qualifiers.size()]));

            if (mockInstance != null)
            {
                return (T)mockInstance;
            }
        }
        return wrapped.produce(creationalContext);
    }

    /*
     * generated
     */

    @Override
    public void inject(T instance, CreationalContext<T> ctx)
    {
        wrapped.inject(instance, ctx);
    }

    @Override
    public void postConstruct(T instance)
    {
        wrapped.postConstruct(instance);
    }

    @Override
    public void preDestroy(T instance)
    {
        wrapped.preDestroy(instance);
    }

    @Override
    public void dispose(T instance)
    {
        wrapped.dispose(instance);
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints()
    {
        return wrapped.getInjectionPoints();
    }
}
