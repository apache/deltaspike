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
package org.apache.deltaspike.jsf.impl.token;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.UUID;

@WindowScoped
@Named("dsPostRequestToken")
public class PostRequestTokenManager implements Serializable
{
    private static final long serialVersionUID = 5387627547198129897L;

    private volatile String currentToken;

    private boolean allowPostRequestWithoutDoubleSubmitPrevention = true;

    protected PostRequestTokenManager()
    {
    }

    @Inject
    public PostRequestTokenManager(JsfModuleConfig config)
    {
        this.allowPostRequestWithoutDoubleSubmitPrevention = config.isAllowPostRequestWithoutDoubleSubmitPrevention();
    }

    public void createNewToken()
    {
        this.currentToken = UUID.randomUUID().toString().replace("-", "");
    }

    public synchronized boolean isValidRequest(String token)
    {
        if (token == null)
        {
            return this.allowPostRequestWithoutDoubleSubmitPrevention;
        }
        String previousToken = this.currentToken;
        createNewToken();

        return token.equals(previousToken);
    }

    public String getCurrentToken()
    {
        return this.currentToken;
    }
}
