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
package org.apache.deltaspike.jsf.impl.navigation;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.ConfigurableNavigationHandlerWrapper;
import javax.faces.application.NavigationCase;
import javax.faces.context.FacesContext;
import java.util.Map;
import java.util.Set;

//ATTENTION: don't rename/move this class as long as we need the workaround in impl-ee6
//(further details are available at: DELTASPIKE-655 and DELTASPIKE-659)

@SuppressWarnings("UnusedDeclaration")
public class DeltaSpikeNavigationHandlerWrapper extends ConfigurableNavigationHandlerWrapper
{
    private final ConfigurableNavigationHandler wrapped;
    private final DeltaSpikeNavigationHandler deltaSpikeNavigationHandler;

    public DeltaSpikeNavigationHandlerWrapper(ConfigurableNavigationHandler wrapped)
    {
        this.wrapped = wrapped;
        //only for delegating the methods implemented by DeltaSpikeNavigationHandler
        this.deltaSpikeNavigationHandler = new DeltaSpikeNavigationHandler(wrapped);
    }

    @Override
    public void handleNavigation(FacesContext context, String fromAction, String outcome)
    {
        this.deltaSpikeNavigationHandler.handleNavigation(context, fromAction, outcome);
    }

    @Override
    public Map<String, Set<NavigationCase>> getNavigationCases()
    {
        return this.deltaSpikeNavigationHandler.getNavigationCases();
    }

    @Override
    public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome)
    {
        return this.deltaSpikeNavigationHandler.getNavigationCase(context, fromAction, outcome);
    }

    public ConfigurableNavigationHandler getWrapped()
    {
        return wrapped;
    }
}
