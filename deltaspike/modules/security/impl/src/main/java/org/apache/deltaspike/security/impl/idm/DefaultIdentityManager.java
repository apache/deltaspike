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
package org.apache.deltaspike.security.impl.idm;

import java.util.Collection;
import java.util.Date;

import org.apache.deltaspike.security.api.idm.Group;
import org.apache.deltaspike.security.api.idm.GroupQuery;
import org.apache.deltaspike.security.api.idm.IdentityManager;
import org.apache.deltaspike.security.api.idm.IdentityType;
import org.apache.deltaspike.security.api.idm.MembershipQuery;
import org.apache.deltaspike.security.api.idm.Role;
import org.apache.deltaspike.security.api.idm.RoleQuery;
import org.apache.deltaspike.security.api.idm.User;
import org.apache.deltaspike.security.api.idm.UserQuery;

/**
 * Default implementation of the IdentityManager interface 
 *
 */
public class DefaultIdentityManager implements IdentityManager
{

    @Override
    public User createUser(String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeUser(User user)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeUser(String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public User getUser(String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<User> getAllUsers()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Group createGroup(String id)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Group createGroup(String id, Group parent)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Group createGroup(String id, String parent)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeGroup(Group group)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeGroup(String groupId)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Group getGroup(String groupId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Group getGroup(String groupId, Group parent)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Group> getAllGroups()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addToGroup(IdentityType identityType, Group group)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeFromGroup(IdentityType identityType, Group group)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Collection<IdentityType> getGroupMembers(Group group)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Role createRole(String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeRole(Role role)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeRole(String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Role getRole(String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Role> getAllRoles()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Role> getRoles(IdentityType identityType, Group group)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasRole(Role role, IdentityType identityType, Group group)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void grantRole(Role role, IdentityType identityType, Group group)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void revokeRole(Role role, IdentityType identityType, Group group)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public UserQuery createUserQuery()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GroupQuery createGroupQuery()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RoleQuery createRoleQuery()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MembershipQuery createMembershipQuery()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean validatePassword(String password)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void updatePassword(String password)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setEnabled(IdentityType identityType, boolean enabled)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setExpirationDate(IdentityType identityType, Date expirationDate)
    {
        // TODO Auto-generated method stub
        
    }

}
