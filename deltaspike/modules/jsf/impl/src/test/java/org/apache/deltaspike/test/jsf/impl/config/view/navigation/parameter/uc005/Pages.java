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
package org.apache.deltaspike.test.jsf.impl.config.view.navigation.parameter.uc005;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameter;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.jsf.api.config.view.View.NavigationMode;
import org.apache.deltaspike.jsf.api.config.view.View.ViewParameterMode;

@View(navigation = NavigationMode.REDIRECT, viewParams = ViewParameterMode.INCLUDE)
public interface Pages extends ViewConfig
{
    @NavigationParameter(key = "param1", value = "staticValue2")
    class Index implements Pages
    {
    }

    @NavigationParameter.List({
            @NavigationParameter(key = "param1", value = "staticValue2"),
            @NavigationParameter(key = "param2", value = "#{pageBean005.currentValue}")
    })
    class Overview implements Pages
    {
    }
}
