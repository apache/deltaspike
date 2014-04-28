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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.parameter.uc006;

import org.apache.deltaspike.core.api.config.view.DefaultErrorView;
import org.apache.deltaspike.core.api.config.view.navigation.ViewNavigationHandler;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

@Model
public class PageBean006
{
    @Inject
    private ViewNavigationHandler viewNavigationHandler;

    //faces-redirect=true will be added by the navigation handler
    public void anyMethod()
    {
        //navigates to the view which is configured as default error-view
        //(in this example via a redirect, because it's configured for PageConfigForRedirect)
        this.viewNavigationHandler.navigateTo(DefaultErrorView.class);
    }
}
