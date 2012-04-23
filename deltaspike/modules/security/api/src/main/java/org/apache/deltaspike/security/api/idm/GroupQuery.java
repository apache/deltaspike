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
 * GroupQuery. All applied conditions will be resolved with logical AND.
 */
public interface GroupQuery
{
    //TODO: Javadocs
    //TODO: Exceptions

    // Operations

    GroupQuery reset();

    GroupQuery immutable();

    List<Group> executeQuery(GroupQuery query);


    // Conditions

    GroupQuery setName(String name);

    String getName();

    GroupQuery setId(String id);

    String getId();

    GroupQuery setParentGroup(Group group);

    GroupQuery setParentGroup(String groupId);

    Group getParentGroup();

    GroupQuery setRole(Role role);

    GroupQuery setRole(String role);

    Role getRole();

    GroupQuery setRelatedUser(User user);

    GroupQuery setRelatedUser(String user);

    User getRelatedUser();

    GroupQuery addAttributeFilter(String name, String[] values);

    Map<String, String[]> getAttributeFilters();

    GroupQuery sort(boolean ascending);

    void setRange(Range range);

    Range getRange();

}
