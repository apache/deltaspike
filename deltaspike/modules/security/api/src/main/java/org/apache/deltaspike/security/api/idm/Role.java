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

/**
 * Role representation
 */
public interface Role extends IdentityType
{
    //TODO: Javadocs
    //TODO: Exceptions
    //TODO: User related methods
    //TODO: Group related methods

    // Self

    String getName();

    boolean exists(User user, Group group);

    boolean exists(String user, String groupId);


    void add(User user, Group group);

    void add(String user, String groupId);


    // Users

    Collection<User> getUsers(Group group);

    Collection<User> getUsers(String groupId);


    // Groups

    Collection<Group> getGroups(User user);

    Collection<Group> getGroups(String user);


}
