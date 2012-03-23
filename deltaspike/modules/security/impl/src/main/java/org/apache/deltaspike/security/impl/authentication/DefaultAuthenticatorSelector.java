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
package org.apache.deltaspike.security.impl.authentication;

import org.apache.deltaspike.core.api.literal.NamedLiteral;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.StringUtils;
import org.apache.deltaspike.security.spi.authentication.Authenticator;
import org.apache.deltaspike.security.spi.authentication.AuthenticatorSelector;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;

/**
 * Default implementation of AuthenticatorSelector
 */
@RequestScoped
public class DefaultAuthenticatorSelector implements AuthenticatorSelector
{
    private String authenticatorName;
    
    private Class<? extends Authenticator> authenticatorClass;
    
    @Inject @Any 
    private Instance<Authenticator> authenticators;
    
    /**
     * Returns an Authenticator instance to be used for authentication. The default
     * implementation obeys the following business logic:
     * <p/>
     * 1. If the user has specified an authenticatorClass property, use it to
     * locate the Authenticator with that exact type
     * 2. If the user has specified an authenticatorName property, use it to
     * locate and return the Authenticator with that name
     * 3. If the authenticatorClass and authenticatorName haven't been specified,
     * and the user has provided their own custom Authenticator, return that one
     * 4. If the user hasn't provided a custom Authenticator, return IdmAuthenticator
     * and attempt to use the Identity Management API to authenticate
     *
     * @return
     */
    public Authenticator getSelectedAuthenticator() 
    {
        if (authenticatorClass != null) 
        {
            return authenticators.select(authenticatorClass).get();
        }

        if (!StringUtils.isEmpty(authenticatorName))
        {
            Instance<Authenticator> selected = authenticators.select(new NamedLiteral(authenticatorName));
            if (selected.isAmbiguous()) 
            {
                //log.error("Multiple Authenticators found with configured name [" + authenticatorName + "]");
                return null;
            }

            if (selected.isUnsatisfied()) 
            {
                //log.error("No authenticator with name [" + authenticatorName + "] was found");
                return null;
            }

            return selected.get();
        }

        Authenticator selectedAuth = null;
        
        List<Authenticator> references = BeanProvider.getContextualReferences(Authenticator.class, true);

        for (Authenticator auth : references) 
        {
            // If the user has provided their own custom authenticator then use it
            if (isExternalAuthenticator(auth.getClass()))
            {
                selectedAuth = auth;
                break;
            }
        }

        if (selectedAuth == null)
        {
            //X TODO discuss default
        }

        return selectedAuth;
    }


    private boolean isExternalAuthenticator(Class<? extends Authenticator> authClass) 
    {
        //X TODO specify the behaviour
        return !authClass.getName().startsWith(getClass().getPackage().getName());
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
