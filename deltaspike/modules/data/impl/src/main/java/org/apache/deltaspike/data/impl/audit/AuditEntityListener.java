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
package org.apache.deltaspike.data.impl.audit;

import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;

public class AuditEntityListener
{

    @PrePersist
    public void persist(Object entity)
    {
        BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(PrePersistAuditListener.class);
        for (Bean<?> bean : beans)
        {
            PrePersistAuditListener result = (PrePersistAuditListener) beanManager.getReference(
                    bean, PrePersistAuditListener.class, beanManager.createCreationalContext(bean));
            result.prePersist(entity);
        }
    }

    @PreUpdate
    public void update(Object entity)
    {
        BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(PreUpdateAuditListener.class);
        for (Bean<?> bean : beans)
        {
            PreUpdateAuditListener result = (PreUpdateAuditListener) beanManager.getReference(
                    bean, PreUpdateAuditListener.class, beanManager.createCreationalContext(bean));
            result.preUpdate(entity);
        }
    }

}
