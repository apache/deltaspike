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

import java.io.Serializable;

/**
 * Represents the identity of the current user, and provides an API for authentication and authorization. 
 *
 */
public interface Identity extends Serializable
{
    public enum AuthenticationResult
    {
        SUCCESS, FAILED
    }
    
    /**
     * Simple check that returns true if the user is logged in, without attempting to authenticate
     *
     * @return true if the user is logged in
     */
    boolean isLoggedIn();

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
     * @return AuthenticationResult returns SUCCESS if user is authenticated,
     * FAILED if authentication FAILED, or
     * EXCEPTION if an EXCEPTION occurred during authentication. These response
     * values may be used to control user navigation.  For deferred authentication methods, such as Open ID
     * the login() method will return an immediate result of FAILED (and subsequently fire
     * a LoginFailedEvent) however in these conditions it is the responsibility of the Authenticator
     * implementation to take over the authentication process, for example by redirecting the user to
     * a third party authentication service such as an OpenID provider.
     */
    AuthenticationResult login();

    /**
     * Logs out the currently authenticated user
     */
    void logout();
}
