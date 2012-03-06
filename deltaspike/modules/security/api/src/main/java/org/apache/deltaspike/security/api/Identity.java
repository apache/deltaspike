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
package org.apache.deltaspike.security.api;

import java.util.Set;

import org.apache.deltaspike.security.api.annotation.LoggedIn;
import org.apache.deltaspike.security.api.annotation.Secures;

/**
 * Represents the identity of the current user, and provides an API for authentication and authorization. 
 *
 */
public interface Identity
{
    public enum AuthenticationResult
    {
        success, failed, exception
    }
    
    /**
     * Simple check that returns true if the user is logged in, without attempting to authenticate
     *
     * @return true if the user is logged in
     */
    @Secures
    @LoggedIn
    boolean isLoggedIn();

    /**
     * Returns true if the currently authenticated user has provided their correct credentials
     * within the verification window configured by the application.
     *
     * @return true if the current user is verified
     */
    boolean isVerified();

    /**
     * Will attempt to authenticate quietly if the user's credentials are set and they haven't
     * authenticated already.  A quiet authentication doesn't throw any exceptions or create any
     * system messages if authentication fails.
     * 
     * This method is intended to be used primarily as an internal API call, however has been made 
     * public for convenience.
     *
     */
    void quietLogin();

    /**
     * Returns the currently authenticated user
     *
     * @return
     */
    User getUser();

    /**
     * Attempts to authenticate the user.  This method raises the following events in response 
     * to whether authentication is successful or not.  The following events may be raised
     * during the call to login():
     * <p/>
     * org.jboss.seam.security.events.LoggedInEvent - raised when authentication is successful
     * org.jboss.seam.security.events.LoginFailedEvent - raised when authentication fails
     * org.jboss.seam.security.events.AlreadyLoggedInEvent - raised if the user is already authenticated
     *
     * @return AuthenticationResult returns success if user is authenticated, 
     * failed if authentication failed, or
     * exception if an exception occurred during authentication. These response
     * values may be used to control user navigation.  For deferred authentication methods, such as Open ID
     * the login() method will return an immediate result of failed (and subsequently fire
     * a LoginFailedEvent) however in these conditions it is the responsibility of the Authenticator
     * implementation to take over the authentication process, for example by redirecting the user to
     * a third party authentication service such as an OpenID provider.
     */
    AuthenticationResult login();

    /**
     * Logs out the currently authenticated user
     */
    void logout();

    /**
     * Checks if the authenticated user is a member of the specified role.
     *
     * @param role String The name of the role to check
     * @return boolean True if the user is a member of the specified role
     */
    boolean hasRole(String role, String group, String groupType);

    /**
     * Checks if the authenticated user is a member of the specified group
     *
     * @param name      The name of the group
     * @param groupType The type of the group, e.g. "office", "department", "global role", etc
     * @return true if the user is a member of the group
     */
    boolean inGroup(String name, String groupType);

    /**
     * Checks if the currently authenticated user has the necessary permission for
     * a specific resource.
     *
     * @return true if the user has the required permission, otherwise false
     */
    boolean hasPermission(Object resource, String permission);

    /**
     * Returns an immutable set containing all the current user's granted roles
     *
     * @return
     */
    Set<Role> getRoles();

    /**
     * Returns an immutable set containing all the current user's group memberships
     *
     * @return
     */
    Set<Group> getGroups();
}
