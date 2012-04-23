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
import java.util.Map;
import java.util.Set;

/**
 * User representation
 */
public interface User extends IdentityObject
{
    //TODO: Javadocs
    //TODO: Exceptions

    //TODO: minimal set of "hard-coded" attributes that make sense:
    //TODO: Personal - First/Last/Full Name, Phone, Email, Organization, Created Date, Birthdate; Too much??

    //TODO: separate UserProfile?

    //TODO: for some of those builtin attributes like email proper validation (dedicated exception?) is needed

    //TODO: authentication - password/token validation

    //TODO: non human identity - another interface?


    // Built in attributes

    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName();

    //TODO: this one could be configurable with some regex
    String getFullName();

    String getEmail();

    void setEmail(String email);

    boolean isEnabled();

    void enable();

    void disable();

    Date getExpirationDate();

    void setExpirationDate(Date expirationDate);

    Date getCreationDate();


    // Roles

    void addRole(Role role, Group group);

    void addRole(String role, String groupId);

    Collection<Role> getRoles(Group group);

    Collection<Role> getRoles(String groupId);

    Map<Role, Set<Group>> getMembershipsMap();

    // TODO: ?? Map<Group, Set<Role>> getMembershipMap() <-- both?

    Collection<Membership> getMemberships();

    Collection<Group> getGroups(Role role);

    Collection<Group> getGroups(String role);

    boolean hasRole(Role role, Group group);

    boolean hasRole(String role, String groupId);


    // Authentication

    // TODO: token stuff
    // TODO: boolean validateToken(Object token); ???

    boolean validatePassword(String password);

    void updatePassword(String password);





}
