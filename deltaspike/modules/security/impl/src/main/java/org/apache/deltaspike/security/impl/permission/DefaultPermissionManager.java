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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.deltaspike.security.api.permission.Permission;
import org.apache.deltaspike.security.api.permission.PermissionManager;
import org.apache.deltaspike.security.api.permission.PermissionQuery;
import org.apache.deltaspike.security.spi.permission.PermissionStore;

/**
 * Default implementation of the PermissionManager interface
 */
@ApplicationScoped
public class DefaultPermissionManager implements PermissionManager
{
    @Inject
    PermissionStore permissionStore;

    @Override
    public PermissionQuery createPermissionQuery()
    {
        PermissionQuery q = new PermissionQuery(permissionStore);
        return q;
    }

    @Override
    public void grantPermission(Permission permission)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void grantPermissions(Collection<Permission> permission)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void revokePermission(Permission permission)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void revokePermissions(Collection<Permission> permissions)
    {
        // TODO Auto-generated method stub
        
    }

}
