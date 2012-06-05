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
package org.apache.deltaspike.security.spi.idm;

import org.apache.deltaspike.security.api.idm.Group;
import org.apache.deltaspike.security.api.idm.GroupQuery;
import org.apache.deltaspike.security.api.idm.Membership;
import org.apache.deltaspike.security.api.idm.MembershipQuery;
import org.apache.deltaspike.security.api.idm.Range;
import org.apache.deltaspike.security.api.idm.Role;
import org.apache.deltaspike.security.api.idm.RoleQuery;
import org.apache.deltaspike.security.api.idm.User;
import org.apache.deltaspike.security.api.idm.UserQuery;

import java.util.List;
import java.util.Map;

/**
 * IdentityStore representation providing minimal SPI
 *
 */
public interface IdentityStore
{
   //TODO: Javadocs
   //TODO: Exceptions 

   //TODO: control hooks, events
   //TODO: authentication, password strenght, salted password hashes


    // User

    User createUser(String name);
   
    void removeUser(User user);
   
    User getUser(String name);
   

    // Group

    Group createGroup(String name, Group parent);
   
    void removeGroup(Group group);
   
    Group getGroup(String name);


    // Role
   
    Role createRole(String name);
   
    void removeRole(Role role);
   
    Role getRole(String role);
 
   
    // Memberships
   
    Membership createMembership(Role role, User user, Group group);
   
    void removeMembership(Role role, User user, Group group);
   
    Membership getMembership(Role role, User user, Group group);
   

    // Queries

    List<User> executeQuery(UserQuery query, Range range);

    List<Group> executeQuery(GroupQuery query, Range range);
 
    List<Role> executeQuery(RoleQuery query, Range range);

    List<Membership> executeQuery(MembershipQuery query, Range range);
   
   
    // Attributes
   
   
    // User

   /**
    * Set attribute with given name and values. Operation will overwrite any previous values.
    * Null value or empty array will remove attribute.
    *
    * @param user
    * @param name of attribute
    * @param values to be set
    */
    void setAttribute(User user, String name, String[] values);

   /**
    * @param user
    * Remove attribute with given name
    *
    * @param name of attribute
    */
    void removeAttribute(User user, String name);

   
   /**
    * @param user
    * @param name of attribute
    * @return attribute values or null if attribute with given name doesn't exist
    */
    String[] getAttributeValues(User user, String name);

   /**
    * @param user
    * @return map of attribute names and their values
    */
    Map<String, String[]> getAttributes(User user);
   
   
   // Group

   /**
    * Set attribute with given name and values. Operation will overwrite any previous values.
    * Null value or empty array will remove attribute.
    *
    * @param group
    * @param name of attribute
    * @param values to be set
    */
    void setAttribute(Group group, String name, String[] values);

   /**
    * Remove attribute with given name
    *
    * @param group
    * @param name of attribute
    */
    void removeAttribute(Group group, String name);


   /**
    * @param group
    * @param name of attribute
    * @return attribute values or null if attribute with given name doesn't exist
    */
    String[] getAttributeValues(Group group, String name);

   /**
    * @param group
    * @return map of attribute names and their values
    */
    Map<String, String[]> getAttributes(Group group);
   
   
   // Role

   /**
    * Set attribute with given name and values. Operation will overwrite any previous values.
    * Null value or empty array will remove attribute.
    *
    * @param role
    * @param name of attribute
    * @param values to be set
    */
    void setAttribute(Role role, String name, String[] values);

   /**
    * Remove attribute with given name
    *
    * @param role
    * @param name of attribute
    */
    void removeAttribute(Role role, String name);


   /**
    * @param role
    * @param name of attribute
    * @return attribute values or null if attribute with given name doesn't exist
    */
    String[] getAttributeValues(Role role, String name);

   /**
    * @param role
    * @return map of attribute names and their values
    */
    Map<String, String[]> getAttributes(Role role);

}
