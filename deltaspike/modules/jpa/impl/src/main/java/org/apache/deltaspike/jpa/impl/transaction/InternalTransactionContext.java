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
package org.apache.deltaspike.jpa.impl.transaction;

import org.apache.deltaspike.core.api.literal.AnyLiteral;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.persistence.EntityManager;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class InternalTransactionContext
{
    private final BeanManager beanManager;

    private Map<String, TransactionMetaDataEntry> transactionMetaDataEntries
        = new HashMap<String, TransactionMetaDataEntry>();

    InternalTransactionContext(BeanManager beanManager)
    {
        this.beanManager = beanManager;
    }

    void addTransactionMetaDataEntry(String key, EntityManager entityManager)
    {
        if (!this.transactionMetaDataEntries.containsKey(key))
        {
            this.transactionMetaDataEntries.put(key, new TransactionMetaDataEntry(key, entityManager));
        }
    }

    void addTransactionMetaDataEntry(Class<? extends Annotation> qualifier)
    {
        addTransactionMetaDataEntry(qualifier.getName(), resolveEntityManagerForQualifier(qualifier));
    }

    Collection<TransactionMetaDataEntry> getTransactionMetaDataEntries()
    {
        return transactionMetaDataEntries.values();
    }

    private EntityManager resolveEntityManagerForQualifier(Class<? extends Annotation> qualifierClass)
    {
        Bean<EntityManager> entityManagerBean = resolveEntityManagerBean(qualifierClass);

        if (entityManagerBean == null)
        {
            return null;
        }

        return (EntityManager) beanManager.getReference(entityManagerBean, EntityManager.class,
                beanManager.createCreationalContext(entityManagerBean));
    }

    protected Bean<EntityManager> resolveEntityManagerBean(Class<? extends Annotation> qualifierClass)
    {
        Set<Bean<?>> entityManagerBeans = beanManager.getBeans(EntityManager.class, new AnyLiteral());
        if (entityManagerBeans == null)
        {
            entityManagerBeans = new HashSet<Bean<?>>();
        }

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
        return null;
    }
}
