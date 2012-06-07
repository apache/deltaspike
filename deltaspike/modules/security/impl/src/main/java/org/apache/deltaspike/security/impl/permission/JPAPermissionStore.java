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

package org.apache.deltaspike.security.impl.permission;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.EntityManager;

import org.apache.deltaspike.security.api.SecurityConfigurationException;
import org.apache.deltaspike.security.api.permission.Permission;
import org.apache.deltaspike.security.api.permission.PermissionQuery;
import org.apache.deltaspike.security.api.permission.annotations.ACLStore;
import org.apache.deltaspike.security.spi.permission.PermissionStore;

/**
 * A PermissionStore implementation backed by a JPA datasource
 *
 */
@ApplicationScoped
public class JPAPermissionStore implements PermissionStore, Extension
{
    private Class<?> generalPermissionStore = null;
    
    private Map<Class<?>, Class<?>> permissionStoreMap = new HashMap<Class<?>, Class<?>>();
    
    @Inject 
    private Instance<EntityManager> entityManagerInstance;
    
    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event,
            final BeanManager beanManager) 
    {
        
        if (event.getAnnotatedType().isAnnotationPresent(Entity.class)) {
            AnnotatedType<X> type = event.getAnnotatedType();
            
            if (type.isAnnotationPresent(ACLStore.class)) 
            {
                ACLStore store = type.getAnnotation(ACLStore.class);
                if (store.value() == null)
                {
                    if (generalPermissionStore == null)
                    {
                        generalPermissionStore = type.getJavaClass();
                    }
                    else
                    {
                        throw new SecurityConfigurationException(
                                "More than one entity bean has been configured as a general ACL store - " +
                                "conflicting bean classes: " + generalPermissionStore.getName() + " and " +
                                type.getJavaClass().getName());
                    }
                }
                else
                {
                    if (permissionStoreMap.containsKey(store.value()))
                    {
                        throw new SecurityConfigurationException(
                                "More than one entity bean has been configured to store ACL permissions for class " +
                                store.value().getName() + " - conflicting classes: " +
                                permissionStoreMap.get(store.value()).getName() + " and " + type.getJavaClass().getName());
                    }
                    else
                    {
                        permissionStoreMap.put(store.value(), type.getJavaClass());
                    }
                }
            }
                
        }
    }

    @Override
    public List<Permission> getPermissions(PermissionQuery query)
    {
        EntityManager em = entityManagerInstance.get();
        
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean grantPermission(Permission permission)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean grantPermissions(Collection<Permission> permissions)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean revokePermission(Permission permission)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean revokePermissions(Collection<Permission> permissions)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> listAvailableActions(Object target)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clearPermissions(Object target)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isEnabled()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
