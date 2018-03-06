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
package org.apache.deltaspike.example.viewconfig;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameter;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.jsf.api.config.view.View.NavigationMode;
import org.apache.deltaspike.jsf.api.config.view.View.ViewParameterMode;
import org.apache.deltaspike.security.api.authorization.Secured;

public interface Pages extends ViewConfig
{
    @View(navigation = NavigationMode.REDIRECT, viewParams = ViewParameterMode.INCLUDE)
    interface RedirectedPages extends ViewConfig
    {
    }

    @Secured(DenyAllAccessDecisionVoter.class)
    interface SecuredPages extends ViewConfig
    {
    }

    @Folder(name = "./viewconfig/")
    interface ViewConfigFolder extends ViewConfig
    {

        class RedirectedPage implements RedirectedPages
        {
        }

        class ViewConfigPage implements RedirectedPages
        {
        }

        @NavigationParameter.List({
                @NavigationParameter(key = "param1", value = "Hey, I come from a navigation parameter"),
                @NavigationParameter(key = "param2", value = "Hey, It's an interpolated value: #{myBean.aValue}")
            })
        class NavigationParameterPage implements RedirectedPages
        {
        }

        class SecuredPage implements SecuredPages
        {
        }
        
        class AllowedPage implements ViewConfig
        {
        }
    }
}
