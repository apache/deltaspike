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
import org.apache.deltaspike.security.api.Credentials;
import org.apache.deltaspike.security.api.Identity;
import org.apache.deltaspike.security.api.Role;
import org.apache.deltaspike.security.api.User;
import org.apache.deltaspike.security.api.events.AlreadyLoggedInEvent;
import org.apache.deltaspike.security.api.events.LoggedInEvent;
import org.apache.deltaspike.security.api.events.LoginFailedEvent;
import org.apache.deltaspike.security.api.events.PostLoggedOutEvent;
import org.apache.deltaspike.security.api.events.PreLoggedOutEvent;
import org.apache.deltaspike.security.api.events.QuietLoginEvent;
import org.apache.deltaspike.security.spi.Authenticator;

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
    
    private User user;
    
    private Class<? extends Authenticator> authenticatorClass;
    
    private String authenticatorName;    
    
    private Set<Role> userRoles = new HashSet<Role>();

    private Set<Group> userGroups = new HashSet<Group>();    

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
        // TODO discuss authentication API
        return false;
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

    public Class<? extends Authenticator> getAuthenticatorClass() 
    {
        return authenticatorClass;
    }

    public void setAuthenticatorClass(Class<? extends Authenticator> authenticatorClass) 
    {
        this.authenticatorClass = authenticatorClass;
    }

    public String getAuthenticatorName() 
    {
        return authenticatorName;
    }

    public void setAuthenticatorName(String authenticatorName) 
    {
        this.authenticatorName = authenticatorName;
    }
}
