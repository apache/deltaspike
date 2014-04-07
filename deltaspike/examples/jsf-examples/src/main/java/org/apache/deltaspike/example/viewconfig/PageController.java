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

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameter;
import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameterContext;
import org.apache.deltaspike.core.api.config.view.navigation.ViewNavigationHandler;
import org.apache.deltaspike.example.viewconfig.Pages.SecuredPages;
import org.apache.deltaspike.example.viewconfig.Pages.ViewConfigFolder.AllowedPage;
import org.apache.deltaspike.example.viewconfig.Pages.ViewConfigFolder.NavigationParameterPage;
import org.apache.deltaspike.example.viewconfig.Pages.ViewConfigFolder.RedirectedPage;
import org.apache.deltaspike.example.viewconfig.Pages.ViewConfigFolder.SecuredPage;
import org.apache.deltaspike.example.viewconfig.Pages.ViewConfigFolder.ViewConfigPage;

@Model
public class PageController
{

    @Inject
    private NavigationParameterContext navigationParameterContext;

    @Inject
    private ViewNavigationHandler viewNavigationHandler;

    public Class<RedirectedPage> toRedirectedPage()
    {
        return RedirectedPage.class;
    }

    public Class<ViewConfigPage> returnToMainPage()
    {
        return ViewConfigPage.class;
    }

    @NavigationParameter(key = "param4", value = "Parameter from the Controller class")
    public Class<NavigationParameterPage> toNavigationParameterPage()
    {
        this.navigationParameterContext.addPageParameter("param3",
                "I also come from a navigation parameter using Dynamic Configuration via NavigationParameterContext");
        return NavigationParameterPage.class;
    }

    public Class<? extends SecuredPages> toSecuredPage()
    {
        return SecuredPage.class;
    }

    public void doAnyActionAndProceeed()
    {
        // action is performed
        this.viewNavigationHandler.navigateTo(AllowedPage.class);
    }

}
