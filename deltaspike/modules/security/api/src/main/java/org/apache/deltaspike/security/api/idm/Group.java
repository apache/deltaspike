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
import java.util.Map;
import java.util.Set;

/**
 * Group representation
 */
public interface Group extends IdentityObject
{
    //TODO: Javadocs
    //TODO: Exceptions

    //TODO: getId() -> getPath()? Should it stick to natural Id(path) or have non meaningful one

    // Self related

    /**
     * Groups are stored in tree hierarchy and therefore ID represents a path. ID string always
     * begins with "/" element that represents root of the tree
     * <p/>
     * Example: Valid IDs are "/acme/departments/marketing", "/security/administrator" or "/administrator".
     * Where "acme", "departments", "marketing", "security" and "administrator" are group names.
     *
     * @return Group Id in String representation.
     */
    String getId();

    /**
     * @return group name
     */
    String getName();


    // Sub groups

    /**
     * @return parent group or null if it refers to root ("/") in a group tree.
     */
    Group getParentGroup();

    /**
     * Creates a new child group.
     *
     * @param name
     */
    void createChildGroup(String name);

    /**
     * Removes child group
     *
     * @param group
     */
    void removeChildGroup(Group group);


    /**
     * Removes child group
     *
     * @param name
     */
    void removeChildGroup(String name);

    /**
     * @return child group. Only groups that exist one level below in the tree will be returned.
     */
    Collection<Group> getChildGroups();


    // Roles


    void addRole(Role role, User user);

    void addRole(String role, String user);

    void removeRole(Role role, User user);

    void removeRole(String role, String user);

    Collection<Role> getRoles(User user);

    Collection<Role> getRoles(String user);

    Collection<User> getUsers(User user);

    Collection<User> getUsers(String user);

    Collection<User> getUsersWithRole(Role role);

    Collection<User> getUsersWithRole(String role);

    Map<Role, Set<User>> getMembershipsMap();

    Collection<Membership> getMemberships();

    boolean hasRole(Role role, User user);

    boolean hasRole(String role, String user);


    // Attributes

    /**
     * Set attribute with given name and value. Operation will overwrite any previous value.
     * Null value will remove attribute.
     *
     * @param name  of attribute
     * @param value to be set
     */
    void setAttribute(String name, String value);

    /**
     * Set attribute with given name and values. Operation will overwrite any previous values.
     * Null value or empty array will remove attribute.
     *
     * @param name   of attribute
     * @param values to be set
     */
    void setAttribute(String name, String[] values);

    /**
     * Remove attribute with given name
     *
     * @param name of attribute
     */
    void removeAttribute(String name);

    /**
     * @param name of attribute
     * @return attribute values or null if attribute with given name doesn't exist. If given attribute has many values
     *         method will return first one
     */
    String getAttribute(String name);

    /**
     * @param name of attribute
     * @return attribute values or null if attribute with given name doesn't exist
     */
    String[] getAttributeValues(String name);

    /**
     * @return map of attribute names and their values
     */
    Map<String, String[]> getAttributes();

}
