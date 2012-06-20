package org.apache.deltaspike.example.securityconsole;

import javax.inject.Inject;

import org.apache.deltaspike.security.api.credential.LoginCredentials;
import org.apache.deltaspike.security.api.idm.SimpleUser;
import org.apache.deltaspike.security.spi.authentication.BaseAuthenticator;

public class BasicAuthenticator extends BaseAuthenticator 
{
    @Inject 
    private LoginCredentials credentials;

    public void authenticate() 
    {
        if ("shane".equals(credentials.getUserId()) &&
                "password".equals(credentials.getCredential().getValue()))
        {
            setUser(new SimpleUser("shane"));
            setStatus(AuthenticationStatus.SUCCESS);
        }
    }
}
