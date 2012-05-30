package org.apache.deltaspike.example.securityconsole;

import javax.inject.Inject;

import org.apache.deltaspike.security.api.User;
import org.apache.deltaspike.security.api.credential.LoginCredential;
import org.apache.deltaspike.security.spi.authentication.BaseAuthenticator;

public class BasicAuthenticator extends BaseAuthenticator 
{
    @Inject 
    private LoginCredential loginCredential;

    public void authenticate() 
    {
        if ("shane".equals(loginCredential.getUserId()) &&
                "password".equals(loginCredential.getCredential().getValue()))
        {
            setUser(new User("shane"));
            setStatus(AuthenticationStatus.SUCCESS);
        }
    }
}
