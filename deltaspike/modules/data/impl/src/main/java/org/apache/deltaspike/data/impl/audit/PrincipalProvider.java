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
import java.util.logging.Level;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.data.api.audit.CreatedBy;
import org.apache.deltaspike.data.api.audit.CurrentUser;
import org.apache.deltaspike.data.api.audit.ModifiedBy;
import org.apache.deltaspike.data.impl.property.Property;

class PrincipalProvider extends AuditProvider
{

    @Inject
    private BeanManager manager;

    @Override
    public void prePersist(Object entity)
    {
        updatePrincipal(entity, true);
    }

    @Override
    public void preUpdate(Object entity)
    {
        updatePrincipal(entity, false);
    }

    private void updatePrincipal(Object entity, boolean create)
    {
        for (Property<Object> property : getProperties(entity, CreatedBy.class, ModifiedBy.class, create))
        {
            setProperty(entity, property, create);
        }
    }

    private void setProperty(Object entity, Property<Object> property, boolean create)
    {
        try
        {
            if (!isCorrectContext(property, create))
            {
                return;
            }
            Object value = resolvePrincipal(entity, property);
            property.setValue(entity, value);
            log.log(Level.FINER, "Updated {0} with {1}", new Object[] { propertyName(entity, property), value });
        }
        catch (Exception e)
        {
            throw new AuditPropertyException("Failed to write principal to " +
                    propertyName(entity, property), e);
        }
    }

    private boolean isCorrectContext(Property<Object> property, boolean create)
    {
        if (create && property.getAnnotatedElement().isAnnotationPresent(ModifiedBy.class))
        {
            ModifiedBy annotation = property.getAnnotatedElement().getAnnotation(ModifiedBy.class);
            if (!annotation.onCreate())
            {
                return false;
            }
        }
        return true;
    }

    protected Object resolvePrincipal(Object entity, Property<Object> property)
    {
        CurrentUser principal = AnnotationInstanceProvider.of(CurrentUser.class);
        Class<?> propertyClass = property.getJavaClass();
        Set<Bean<?>> beans = manager.getBeans(propertyClass, principal);
        if (!beans.isEmpty() && beans.size() == 1)
        {
            Bean<?> bean = beans.iterator().next();
            Object result = manager.getReference(bean, propertyClass, manager.createCreationalContext(bean));
            return result;
        }
        throw new IllegalArgumentException("Principal " + (beans.isEmpty() ? "not found" : "not unique") +
                " for " + propertyName(entity, property));
    }

}
