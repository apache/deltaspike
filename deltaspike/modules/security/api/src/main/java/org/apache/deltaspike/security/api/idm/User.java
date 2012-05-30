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
 * User representation
 */
public interface User extends IdentityType
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

    String getId();

    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName(String lastName);

    //TODO: this one could be configurable with some regex
    String getFullName();

    String getEmail();

    void setEmail(String email);
}
