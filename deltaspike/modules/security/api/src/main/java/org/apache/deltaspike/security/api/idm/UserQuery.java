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

import java.util.List;
import java.util.Map;

/**
 * UserQuery. All applied conditions will be resolved with logical AND.
 */
public interface UserQuery
{
    //TODO: Javadocs
    //TODO: Exceptions

    //TODO: add searchBy stuff that makes sense: email, first/last/full name, organization?

    //TODO: make clear comment in javadoc about usage of wildcards -
    //TODO: should support at least usage of '*' for all built in attributes mentioned above.



    // Operations

    UserQuery reset();

    UserQuery getImmutable();

    List<User> executeQuery(UserQuery query);


    // Conditions

    UserQuery setName(String name);

    String getName();

    UserQuery setRelatedGroup(Group group);

    UserQuery setRelatedGroup(String groupId);

    Group getRelatedGroup();

    UserQuery setRole(Role role);

    UserQuery setRole(String name);

    Role getRole();

    UserQuery setAttributeFilter(String name, String[] values);

    Map<String, String[]> getAttributeFilters();

    // Built in attributes

    UserQuery setFirstName(String firstName);

    String getFirstName();

    UserQuery setLastName(String lastName);

    String getLastName();

    UserQuery setEmail(String email);

    String getEmail();

    UserQuery setEnabled(boolean enabled);

    boolean getEnabled();


    // Pagination

    UserQuery sort(boolean ascending);

    void setRange(Range range);

    Range getRange();

}
