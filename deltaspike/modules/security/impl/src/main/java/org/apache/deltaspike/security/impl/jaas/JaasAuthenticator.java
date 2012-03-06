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
package org.apache.deltaspike.security.impl.jaas;

import java.io.IOException;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.management.relation.Role;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.deltaspike.security.api.BaseAuthenticator;
import org.apache.deltaspike.security.api.Credentials;
import org.apache.deltaspike.security.api.Group;
import org.apache.deltaspike.security.api.Identity;
import org.apache.deltaspike.security.impl.PasswordCredential;
import org.apache.deltaspike.security.spi.Authenticator;

/**
 * An authenticator for authenticating with JAAS.  The jaasConfigName property
 * _must_ be configured to point to a valid JAAS configuration name, typically
 * defined in a file called login-config.xml in the application server.
 */
@Named
@RequestScoped
public class JaasAuthenticator extends BaseAuthenticator implements Authenticator 
{
    @Inject
    private Identity identity;
    
    @Inject
    private Credentials credentials;
    
    @Inject
    private BeanManager manager;

    private Subject subject;

    private String jaasConfigName = null;

    public JaasAuthenticator() 
    {
        subject = new Subject();
    }

    public void authenticate() 
    {
        if (getJaasConfigName() == null) 
        {
            throw new IllegalStateException(
                    "jaasConfigName cannot be null.  Please set it to a valid JAAS configuration name.");
        }

        try 
        {
            getLoginContext().login();
            setStatus(AuthenticationStatus.SUCCESS);
        } 
        catch (LoginException e) 
        {
            setStatus(AuthenticationStatus.FAILURE);
            //log.error("JAAS authentication failed", e);
        }
    }

    protected LoginContext getLoginContext() throws LoginException 
    {
        return new LoginContext(getJaasConfigName(), subject,
                createCallbackHandler());
    }

    /**
     * Creates a callback handler that can handle a standard username/password
     * callback, using the credentials username and password properties
     */
    public CallbackHandler createCallbackHandler() 
    {
        return new CallbackHandler() 
        {
            public void handle(Callback[] callbacks)
                throws IOException, UnsupportedCallbackException 
            {
                for (int i = 0; i < callbacks.length; i++) 
                {
                    if (callbacks[i] instanceof NameCallback) 
                    {
                        ((NameCallback) callbacks[i]).setName(credentials.getUsername());
                    } 
                    else if (callbacks[i] instanceof PasswordCallback) 
                    {
                        if (credentials.getCredential() instanceof PasswordCredential) 
                        {
                            PasswordCredential credential = (PasswordCredential) credentials.getCredential();
                            ((PasswordCallback) callbacks[i]).setPassword(credential.getValue() != null ?
                                    credential.getValue().toCharArray() : null);
                        }
                    } 
                    else 
                    {
                        //log.warn("Unsupported callback " + callbacks[i]);
                    }
                }
            }
        };
    }

    public String getJaasConfigName() 
    {
        return jaasConfigName;
    }

    public void setJaasConfigName(String jaasConfigName) 
    {
        this.jaasConfigName = jaasConfigName;
    }

    public void postAuthenticate() 
    {
    }

    @Override
    public Set<Role> getRoleMemberships()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Group> getGroupMemberships()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
