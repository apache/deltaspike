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
package org.apache.deltaspike.security.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.apache.deltaspike.security.Group;
import org.apache.deltaspike.security.api.AuthenticationException;
import org.apache.deltaspike.security.api.AuthenticatorSelector;
import org.apache.deltaspike.security.api.Credentials;
import org.apache.deltaspike.security.api.Identity;
import org.apache.deltaspike.security.api.Role;
import org.apache.deltaspike.security.api.User;
import org.apache.deltaspike.security.api.events.AlreadyLoggedInEvent;
import org.apache.deltaspike.security.api.events.LoggedInEvent;
import org.apache.deltaspike.security.api.events.LoginFailedEvent;
import org.apache.deltaspike.security.api.events.PostAuthenticateEvent;
import org.apache.deltaspike.security.api.events.PostLoggedOutEvent;
import org.apache.deltaspike.security.api.events.PreAuthenticateEvent;
import org.apache.deltaspike.security.api.events.PreLoggedOutEvent;
import org.apache.deltaspike.security.api.events.QuietLoginEvent;
import org.apache.deltaspike.security.spi.Authenticator;
import org.apache.deltaspike.security.spi.Authenticator.AuthenticationStatus;

/**
 * Default Identity implementation
 */
public class IdentityImpl implements Identity
{
    @Inject 
    private BeanManager beanManager;
    
    @Inject
    private Credentials credentials;
    
    @Inject 
    private Instance<RequestSecurityState> requestSecurityState;
    
    @Inject
    private Instance<AuthenticatorSelector> authenticatorSelector;
    
    private Authenticator activeAuthenticator;
    
    private User user;
    
    private Set<Role> userRoles = new HashSet<Role>();

    private Set<Group> userGroups = new HashSet<Group>();    
    
    /**
     * Flag indicating whether we are currently authenticating
     */
    private boolean authenticating;

    public boolean isLoggedIn() 
    {
        // If there is a user set, then the user is logged in.
        return user != null;
    }    

    public boolean isVerified() 
    {
        // TODO Discuss user verification requirements
        return false;
    }
    
    public User getUser() 
    {
        return user;
    }    

    public void quietLogin() 
    {
        try 
        {
            beanManager.fireEvent(new QuietLoginEvent());

            // Ensure that we haven't been authenticated as a result of the EVENT_QUIET_LOGIN event
            if (!isLoggedIn()) 
            {
                if (credentials.isSet()) 
                {
                    authenticate();

                    if (isLoggedIn()) 
                    {
                        requestSecurityState.get().setSilentLogin(true);
                    }
                }
            }
        } 
        catch (Exception ex) 
        {
            //log.error("Error while authenticating", ex);
            credentials.invalidate();
        }
    }

    @Override
    public AuthenticationResult login() 
    {
        try 
        {
            if (isLoggedIn()) 
            {
                // If authentication has already occurred during this request via a silent login,
                // and login() is explicitly called then we still want to raise the LOGIN_SUCCESSFUL event,
                // and then return.
                if (requestSecurityState.get().isSilentLogin()) 
                {
                    beanManager.fireEvent(new LoggedInEvent(user));
                    return AuthenticationResult.success;
                }

                beanManager.fireEvent(new AlreadyLoggedInEvent());
                return AuthenticationResult.success;
            }

            boolean success = authenticate();

            if (success) 
            {
                //if (log.isDebugEnabled()) 
                //{
                //    log.debug("Login successful");
                //}
                beanManager.fireEvent(new LoggedInEvent(user));
                return AuthenticationResult.success;
            }

            beanManager.fireEvent(new LoginFailedEvent(null));
            return AuthenticationResult.failed;
        } 
        catch (Exception ex) 
        {
            //log.error("Login failed", ex);

            beanManager.fireEvent(new LoginFailedEvent(ex));

            return AuthenticationResult.exception;
        }
    }
    
    protected boolean authenticate() throws AuthenticationException 
    {
        if (authenticating) 
        {
            authenticating = false;
            throw new IllegalStateException("Authentication already in progress.");
        }

        try 
        {
            authenticating = true;

            user = null;

            beanManager.fireEvent(new PreAuthenticateEvent());

            activeAuthenticator = authenticatorSelector.get().getSelectedAuthenticator();

            if (activeAuthenticator == null) 
            {
                authenticating = false;
                throw new AuthenticationException("An Authenticator could not be located");
            }

            activeAuthenticator.authenticate();

            if (activeAuthenticator.getStatus() == null) 
            {
                throw new AuthenticationException("Authenticator must return a valid authentication status");
            }

            switch (activeAuthenticator.getStatus()) 
            {
                case SUCCESS:
                    postAuthenticate();
                    return true;
                case FAILURE:
                default:
                    authenticating = false;
                    return false;
            }
        } 
        catch (Exception ex) 
        {
            authenticating = false;
            if (ex instanceof AuthenticationException) 
            {
                throw (AuthenticationException) ex;
            } 
            else 
            {
                throw new AuthenticationException("Authentication failed.", ex);
            }
        }
    }    
    
    protected void postAuthenticate() 
    {
        if (activeAuthenticator == null) 
        {
            throw new IllegalStateException("activeAuthenticator is null");
        }

        try 
        {
            activeAuthenticator.postAuthenticate();

            if (!activeAuthenticator.getStatus().equals(AuthenticationStatus.SUCCESS))
            {
                return;
            }

            user = activeAuthenticator.getUser();

            if (user == null) 
            {
                throw new AuthenticationException(
                        "Authenticator must provide a non-null User after successful authentication");
            }

            if (isLoggedIn()) 
            {
                // TODO rewrite this once we decide how user privilege state is managed
                
                /**if (!preAuthenticationRoles.isEmpty()) 
                {
                    for (String group : preAuthenticationRoles.keySet()) 
                    {
                        Map<String, List<String>> groupTypeRoles = preAuthenticationRoles.get(group);
                        for (String groupType : groupTypeRoles.keySet()) 
                        {
                            for (String roleType : groupTypeRoles.get(groupType)) 
                            {
                                addRole(roleType, group, groupType);
                            }
                        }
                    }
                    preAuthenticationRoles.clear();
                }

                if (!preAuthenticationGroups.isEmpty()) 
                {
                    for (String group : preAuthenticationGroups.keySet()) 
                    {
                        for (String groupType : preAuthenticationGroups.get(group)) 
                        {
                            activeGroups.add(new SimpleGroup(group, groupType));
                        }
                    }
                    preAuthenticationGroups.clear();
                }*/
            }

            beanManager.fireEvent(new PostAuthenticateEvent());
        } 
        finally 
        {
            // Set credential to null whether authentication is successful or not
            activeAuthenticator = null;
            credentials.setCredential(null);
            authenticating = false;
        }
    }    

    @Override
    public void logout() 
    {
        if (isLoggedIn()) 
        {
            PostLoggedOutEvent loggedOutEvent = new PostLoggedOutEvent(user);

            beanManager.fireEvent(new PreLoggedOutEvent());
            unAuthenticate();

            // TODO invalidate the session
            //session.invalidate();

            beanManager.fireEvent(loggedOutEvent);
        }        
    }
    
    /**
     * Resets all security state and credentials
     */
    public void unAuthenticate() 
    {    
        user = null;
        credentials.clear();
        userRoles.clear();
        userGroups.clear();
    }    

    public boolean hasRole(String role, String group, String groupType) 
    {
        // TODO discuss user/role/group API
        return false;
    }

    public boolean inGroup(String name, String groupType) 
    {
        // TODO discuss user/role/group API
        return false;
    }

    public boolean hasPermission(Object resource, String permission) 
    {
        // TODO discuss user/role/group API
        return false;
    }

    public Set<Role> getRoles() 
    {
        return Collections.unmodifiableSet(userRoles);
    }

    public Set<Group> getGroups() 
    {
        return Collections.unmodifiableSet(userGroups);
    }
}
