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


/**
 * Group representation
 */
public interface Group extends IdentityType
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
     * Group name is unique identifier in specific group tree branch. For example
     * group with id "/acme/departments/marketing" will have name "marketing" and
     * parent group of id "/acme/departments"
     *
     * @return name
     */
    String getName();

    // Sub groups

    /**
     * @return parent group or null if it refers to root ("/") in a group tree.
     */
    Group getParentGroup();

}
