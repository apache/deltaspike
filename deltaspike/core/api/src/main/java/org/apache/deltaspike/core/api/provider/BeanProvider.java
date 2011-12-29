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
package org.apache.deltaspike.core.api.provider;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

/**
 * This class contains utility methods to resolve contextual references
 * in situations where no injection is available.
 *
 * @see BeanManagerProvider
 */
@Typed()
public final class BeanProvider
{
    private BeanProvider()
    {
        // this is a utility class which doesn't get instantiated.
    }

    /**
     * <p></p>Get a Contextual Reference by it's type and annotation.
     * You can use this method to get contextual references of a given type.
     * A 'Contextual Reference' is a proxy which will automatically resolve
     * the correct contextual instance when you access any method.</p>
     *
     * <p><b>Attention:</b> You shall not use this method to manually resolve a
     * &#064;Dependent bean! The reason is that this contextual instances do usually
     * live in the well defined lifecycle of their injection point (the bean they got
     * injected into). But if we manually resolve a &#064;Dependent bean, then it does <b>not</b>
     * belong to such a well defined lifecycle (because &#064;Dependent it is not
     * &#064;NormalScoped) and thus will not automatically be
     * destroyed at the end of the lifecycle. You need to manually destroy this contextual instance via
     * {@link javax.enterprise.context.spi.Contextual#destroy(Object, javax.enterprise.context.spi.CreationalContext)}.
     * Thus you also need to manually store the CreationalContext and the Bean you
     * used to create the contextual instance which this method will not provide.</p>
     *
     * @param type the type of the bean in question
     * @param optional if <code>true</code> it will return <code>null</code> if no bean could be found or created.
     *                 Otherwise it will throw an {@code IllegalStateException}
     * @param qualifiers additional qualifiers which further distinct the resolved bean
     * @param <T> target type
     * @return the resolved Contextual Reference
     */
    public static <T> T getContextualReference(Class<T> type, boolean optional, Annotation... qualifiers)
    {
        BeanManager beanManager = getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(type, qualifiers);

        if (beans == null || beans.isEmpty())
        {
            if (optional)
            {
                return null;
            }

            throw new IllegalStateException("Could not find beans for Type=" + type
                    + " and qualifiers:" + Arrays.toString(qualifiers));
        }

        return getContextualReference(type, beanManager, beans);
    }

    /**
     * <p>Get a Contextual Reference by it's EL Name.
     * This only works for beans with the &#064;Named annotation.</p>
     *
     * <p><b>Attention:</b> please see the notes on manually resolving &#064;Dependent bean
     * in {@link #getContextualReference(Class, boolean, java.lang.annotation.Annotation...)}!</p>
     *
     *
     * @param type the type of the bean in question - only use Object.class if the type is unknown in dyn. use-cases
     * @param optional if <code>true</code> it will return <code>null</code> if no bean could be found or created.
     *                 Otherwise it will throw an {@code IllegalStateException}
     * @param name the EL name of the bean
     * @param <T> target type
     * @return the resolved Contextual Reference
     */
    public static <T> T getContextualReference(Class<T> type, boolean optional, String name)
    {
        BeanManager beanManager = getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(name);

        if (beans == null || beans.isEmpty())
        {
            if (optional)
            {
                return null;
            }

            throw new IllegalStateException("Could not find beans for Type=" + type
                    + " and name:" + name);
        }

        return getContextualReference(type, beanManager, beans);
    }

    /**
     * Internal helper method to resolve the right bean and
     * resolve the contextual reference.
     * @param type the type of the bean in question
     * @param beanManager current bean-manager
     * @param beans beans in question
     * @param <T> target type
     * @return the contextual reference
     */
    private static <T> T getContextualReference(Class<T> type, BeanManager beanManager, Set<Bean<?>> beans)
    {
        Bean<?> bean = beanManager.resolve(beans);

        CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);

        @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
        T result = (T)beanManager.getReference(bean, type, creationalContext);
        return result;
    }

    /**
     * Internal method to resolve the BeanManager via the {@link BeanManagerProvider}
     */
    private static BeanManager getBeanManager()
    {
        return BeanManagerProvider.getInstance().getBeanManager();
    }
}
