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
package org.apache.deltaspike.example.security.requestedpage.cdi;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class LoginController
{
    public static final String DEFAULT_USER = "john";

    public static final String DEFAULT_PASSWORD = "123456";

    public static final String DEFAULT_NAME = "John User";

    private String username;

    private String password;

    private boolean loggedIn = false;

    @Inject
    private Event<UserEvent.LoggedIn> evtLoggedIn;

    @Inject
    private Event<UserEvent.LoginFailed> evtLoginFailed;

    public void login()
    {
        if (DEFAULT_USER.equals(username) && DEFAULT_PASSWORD.equals(password))
        {
            loggedIn = true;
            evtLoggedIn.fire(new UserEvent.LoggedIn());
            System.err.println("logged in");
        }
        else
        {
            evtLoginFailed.fire(new UserEvent.LoginFailed());
            System.err.println("failed");
        }
    }

    public void logout()
    {
        loggedIn = false;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getName()
    {
        return DEFAULT_NAME;
    }

    public boolean isLoggedIn()
    {
        return loggedIn;
    }
}
