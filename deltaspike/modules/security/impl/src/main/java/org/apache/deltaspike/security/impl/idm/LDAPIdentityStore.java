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

import java.util.List;
import java.util.Map;

import org.apache.deltaspike.security.api.idm.Group;
import org.apache.deltaspike.security.api.idm.GroupQuery;
import org.apache.deltaspike.security.api.idm.Membership;
import org.apache.deltaspike.security.api.idm.MembershipQuery;
import org.apache.deltaspike.security.api.idm.Range;
import org.apache.deltaspike.security.api.idm.Role;
import org.apache.deltaspike.security.api.idm.RoleQuery;
import org.apache.deltaspike.security.api.idm.User;
import org.apache.deltaspike.security.api.idm.UserQuery;
import org.apache.deltaspike.security.spi.idm.IdentityStore;

/**
 * An IdentityStore implementation backed by an LDAP directory 
 *
 */
public class LDAPIdentityStore implements IdentityStore
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
    public User getUser(String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Group createGroup(String name, Group parent)
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
    public Group getGroup(String name)
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
    public Role getRole(String role)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Membership createMembership(Role role, User user, Group group)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeMembership(Role role, User user, Group group)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Membership getMembership(Role role, User user, Group group)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<User> executeQuery(UserQuery query, Range range)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Group> executeQuery(GroupQuery query, Range range)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Role> executeQuery(RoleQuery query, Range range)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Membership> executeQuery(MembershipQuery query, Range range)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAttribute(User user, String name, String[] values)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeAttribute(User user, String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String[] getAttributeValues(User user, String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String[]> getAttributes(User user)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAttribute(Group group, String name, String[] values)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeAttribute(Group group, String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String[] getAttributeValues(Group group, String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String[]> getAttributes(Group group)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAttribute(Role role, String name, String[] values)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeAttribute(Role role, String name)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String[] getAttributeValues(Role role, String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String[]> getAttributes(Role role)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
