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

package org.apache.deltaspike.jpa.spi.entitymanager;

import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.jpa.api.entitymanager.EntityManagerResolver;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.persistence.EntityManager;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class QualifierBackedEntityManagerResolver implements EntityManagerResolver
{
    private final Class<? extends Annotation>[] qualifiers;
    private final BeanManager beanManager;

    public QualifierBackedEntityManagerResolver(BeanManager beanManager, Class<? extends Annotation>... qualifiers)
    {
        this.beanManager = beanManager;
        this.qualifiers = qualifiers;
    }

    @Override
    public EntityManager resolveEntityManager()
    {
        Bean<EntityManager> entityManagerBean = resolveEntityManagerBeans();

        if (entityManagerBean == null)
        {
            StringBuilder qualifierNames = new StringBuilder();
            for (Class<?> c : qualifiers)
            {
                qualifierNames.append(c.getName()).append(" ");
            }
            throw new IllegalStateException("Cannot find an EntityManager qualified with [" + qualifierNames
                    + "]. Did you add a corresponding producer?");
        }

        return (EntityManager) beanManager.getReference(entityManagerBean, EntityManager.class,
                beanManager.createCreationalContext(entityManagerBean));
    }
    private Bean<EntityManager> resolveEntityManagerBeans()
    {
        Set<Bean<?>> entityManagerBeans = beanManager.getBeans(EntityManager.class, new AnyLiteral());
        if (entityManagerBeans == null)
        {
            entityManagerBeans = new HashSet<Bean<?>>();
        }
        for (Class<? extends Annotation> qualifierClass : qualifiers)
        {
            for (Bean<?> currentEntityManagerBean : entityManagerBeans)
            {
                Set<Annotation> foundQualifierAnnotations = currentEntityManagerBean.getQualifiers();

                for (Annotation currentQualifierAnnotation : foundQualifierAnnotations)
                {
                    if (currentQualifierAnnotation.annotationType().equals(qualifierClass))
                    {
                        return (Bean<EntityManager>) currentEntityManagerBean;
                    }
                }
            }
        }
        return null;
    }
}
