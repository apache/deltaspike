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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.syntax.uc004;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.jsf.api.config.view.View;

import static org.apache.deltaspike.jsf.api.config.view.View.Extension.JSP;
import static org.apache.deltaspike.jsf.api.config.view.View.NavigationMode.FORWARD;
import static org.apache.deltaspike.jsf.api.config.view.View.NavigationMode.REDIRECT;
import static org.apache.deltaspike.jsf.api.config.view.View.ViewParameterMode.INCLUDE;

interface Pages
{
    @View(name = "home", extension = JSP) class Index implements ViewConfig
    {
    }

    @View(navigation = REDIRECT, viewParams = INCLUDE)
    interface Admin extends ViewConfig
    {
        interface Statistics
        {
            @View
            class Index implements ViewConfig
            {
            }

            class Home implements Admin //inherit navigation = REDIRECT and viewParams = INCLUDE
            {
            }
        }

        class Index implements Admin //inherit navigation = REDIRECT and viewParams = INCLUDE
        {
        }

        @View(navigation = FORWARD)
        class Home implements Admin //inherit navigation = REDIRECT - but overruled and viewParams = INCLUDE
        {
        }
    }
}
