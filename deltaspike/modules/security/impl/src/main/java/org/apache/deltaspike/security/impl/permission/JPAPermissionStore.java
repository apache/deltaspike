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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.deltaspike.security.api.permission.Permission;
import org.apache.deltaspike.security.api.permission.PermissionQuery;
import org.apache.deltaspike.security.impl.permission.JPAPermissionStoreConfig.StoreMetadata;
import org.apache.deltaspike.security.spi.permission.PermissionStore;

/**
 * A PermissionStore implementation backed by a JPA datasource
 *
 */
@ApplicationScoped
public class JPAPermissionStore implements PermissionStore
{   
    @Inject 
    private Instance<EntityManager> entityManagerInstance;    
    
    @Inject 
    private JPAPermissionStoreConfig config;

    @Override
    public List<Permission> getPermissions(PermissionQuery query)
    {
        EntityManager em = entityManagerInstance.get();
        
        Map<StoreMetadata, Set<Object>> resourceMetadata = new HashMap<StoreMetadata, Set<Object>>();                
                
        if (query.getResources() != null)
        {            
            for (Object resource : query.getResources()) 
            {
                Class<?> resourceClass = resource.getClass();
                StoreMetadata meta = (config.getStores().containsKey(resourceClass)) ? 
                        config.getStores().get(resourceClass) : config.getGeneralStore();
                
                if (!resourceMetadata.containsKey(meta))
                {
                    resourceMetadata.put(meta, new HashSet<Object>());
                }
                resourceMetadata.get(meta).add(resource);
            }
        }
        else if (query.getResource() != null)
        {
            Class<?> resourceClass = query.getResource().getClass();
            StoreMetadata meta = (config.getStores().containsKey(resourceClass)) ? 
                    config.getStores().get(resourceClass) : config.getGeneralStore();
            
            if (!resourceMetadata.containsKey(meta))
            {
                resourceMetadata.put(meta, new HashSet<Object>());
            }
            resourceMetadata.get(meta).add(query.getResource());
        }
                
        if (resourceMetadata.isEmpty())
        {
            // No resources specified in query - we need to query every known permission store and retrieve
            // all permissions for the specified query parameters
            
            for (StoreMetadata meta : config.getStores().values())
            {
                Query permissionQuery = buildPermissionQuery(meta, query, em);
                
            }
        }
        else
        {
            List<Permission> results = new ArrayList<Permission>();
            
            // Iterate through each permission store and execute a separate query
            for (StoreMetadata meta : resourceMetadata.keySet())
            {
                Query permissionQuery = buildPermissionQuery(meta, query, em);
            }
        }
                
        // TODO Auto-generated method stub
        return null;
    }
    
    private Query buildPermissionQuery(StoreMetadata meta, PermissionQuery query, EntityManager em)
    {                
        Map<String,Object> paramValues = new HashMap<String,Object>();
        
        StringBuilder queryText = new StringBuilder();
        queryText.append("SELECT P FROM ");
        queryText.append(meta.getStoreClass().getName());
        queryText.append(" P WHERE ");
        
        if (query.getResource() != null)
        {
            queryText.append(meta.getAclIdentifier().getName());
            queryText.append(" = :IDENTIFIER");
            // TODO determine the identifier value
            paramValues.put("IDENTIFIER", null);
        }
        else if (query.getResources() != null)
        {
            
        }
        
        if (query.getRecipient() != null)
        {
            queryText.append(meta.getAclRecipient().getName());
            queryText.append(" = :RECIPIENT");      
            paramValues.put("RECIPIENT", query.getRecipient().getKey());
        }
        
        Query q = em.createQuery(queryText.toString());
        
        for (String param : paramValues.keySet())
        {
            q.setParameter(param, paramValues.get(param));
        }            
        
        // TODO apply the range if specified
        
        return q;
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
