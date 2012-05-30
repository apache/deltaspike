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
package org.apache.deltaspike.security.api.idm;

import java.util.Collection;
import java.util.Date;

/**
 * IdentityManager
 */
public interface IdentityManager
{
    //TODO: Javadocs

    //TODO: Exceptions

    //TODO: control hooks & events

    //TODO: linking identities


    // User

    User createUser(String name);

    void removeUser(User user);

    void removeUser(String name);

    User getUser(String name);

    Collection<User> getAllUsers();


    // Group

    Group createGroup(String id);

    Group createGroup(String id, Group parent);

    Group createGroup(String id, String parent);

    void removeGroup(Group group);

    void removeGroup(String groupId);

    Group getGroup(String groupId);

    Group getGroup(String groupId, Group parent);

    Collection<Group> getAllGroups();
    
    void addToGroup(IdentityType identityType, Group group);
    
    void removeFromGroup(IdentityType identityType, Group group);

    Collection<IdentityType> getGroupMembers(Group group);   

    // Roles

    Role createRole(String name);

    void removeRole(Role role);

    void removeRole(String name);

    Role getRole(String name);

    Collection<Role> getAllRoles();

    Collection<Role> getRoles(IdentityType identityType, Group group);

    boolean hasRole(Role role, IdentityType identityType, Group group);
    
    void grantRole(Role role, IdentityType identityType, Group group);
    
    void revokeRole(Role role, IdentityType identityType, Group group);

    // Queries

    UserQuery createUserQuery();

    GroupQuery createGroupQuery();

    RoleQuery createRoleQuery();

    MembershipQuery createMembershipQuery();

    // Password Management
    
    boolean validatePassword(String password);

    void updatePassword(String password);
    
    // User / Role / Group enablement / expiry

    void setEnabled(IdentityType identityType, boolean enabled);    

    void setExpirationDate(IdentityType identityType, Date expirationDate);    
}
