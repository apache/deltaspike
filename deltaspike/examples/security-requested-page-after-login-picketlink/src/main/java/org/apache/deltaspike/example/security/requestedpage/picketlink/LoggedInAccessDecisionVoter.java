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
package org.apache.deltaspike.example.security.requestedpage.picketlink;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.security.api.authorization.AbstractAccessDecisionVoter;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoterContext;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;
import org.picketlink.Identity;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.util.Set;

@SessionScoped //or @WindowScoped
public class LoggedInAccessDecisionVoter extends AbstractAccessDecisionVoter
{

    @Inject
    private ViewConfigResolver viewConfigResolver;

    @Inject
    private Identity identity;

    // set a default
    private Class<? extends ViewConfig> deniedPage = Pages.Secure.Home.class;

    @Override
    protected void checkPermission(AccessDecisionVoterContext context, Set<SecurityViolation> violations)
    {

        if (identity.isLoggedIn())
        {
            // no violations, pass
        }
        else
        {
            violations.add(new SecurityViolation()
            {
                @Override
                public String getReason()
                {
                    return "User must be logged in to access this resource";
                }
            });

            // remember the requested page
            deniedPage = viewConfigResolver
                    .getViewConfigDescriptor(FacesContext.getCurrentInstance().getViewRoot().getViewId())
                    .getConfigClass();
        }
    }

    public Class<? extends ViewConfig> getDeniedPage()
    {
        return deniedPage;
    }
}
