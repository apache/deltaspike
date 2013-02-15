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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.syntax.uc005;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.jsf.api.config.view.View;

import static org.apache.deltaspike.jsf.api.config.view.View.NavigationMode.FORWARD;
import static org.apache.deltaspike.jsf.api.config.view.View.NavigationMode.REDIRECT;
import static org.apache.deltaspike.jsf.api.config.view.View.ViewParameterMode.INCLUDE;

@View(navigation = REDIRECT)
interface Pages extends ViewConfig
{
    //inherits navigation = REDIRECT
    class Index implements Pages
    {
    }

    interface Admin extends Pages
    {
        //inherits navigation = REDIRECT
        @View(viewParams = INCLUDE)
        interface Statistics extends Admin
        {
            //inherits navigation = REDIRECT and viewParams = INCLUDE
            @View
            class Index implements Statistics
            {
            }

            //inherits navigation = REDIRECT
            class Home implements Admin
            {
            }
        }

        //inherits navigation = REDIRECT
        @View
        class Index implements Admin
        {
        }

        //inherits but overrules navigation
        @View(navigation = FORWARD)
        class Home implements Admin
        {
        }
    }
}
