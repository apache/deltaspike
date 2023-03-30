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
package org.apache.deltaspike.jsf.impl.injection;

import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;
import org.apache.deltaspike.jsf.impl.security.SecurityAwareViewHandler;

import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationWrapper;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PreDestroyViewMapEvent;
import jakarta.faces.event.SystemEvent;

public class InjectionAwareApplicationWrapper extends ApplicationWrapper
{
    private final Application wrapped;
    private final boolean preDestroyViewMapEventFilterMode;
    private final ProjectStage projectStage;

    public InjectionAwareApplicationWrapper(
            Application wrapped, JsfModuleConfig jsfModuleConfig, boolean preDestroyViewMapEventFilterMode,
            ProjectStage projectStage)
    {
        this.wrapped = wrapped;
        this.preDestroyViewMapEventFilterMode = preDestroyViewMapEventFilterMode;
        this.projectStage = projectStage;
    }

    @Override
    public ProjectStage getProjectStage()
    {
        if (this.projectStage == null)
        {
            return getWrapped().getProjectStage();
        }
        return this.projectStage;
    }

    @Override
    public void publishEvent(FacesContext facesContext, Class<? extends SystemEvent> systemEventClass, Object source)
    {
        if (!PreDestroyViewMapEvent.class.isAssignableFrom(systemEventClass) ||
                isPreDestroyViewMapEventAllowed(facesContext))
        {
            super.publishEvent(facesContext, systemEventClass, source);
        }
    }

    private boolean isPreDestroyViewMapEventAllowed(FacesContext facesContext)
    {
        return !this.preDestroyViewMapEventFilterMode ||
                    !Boolean.TRUE.equals(facesContext.getExternalContext().getRequestMap().get(
                            SecurityAwareViewHandler.PRE_DESTROY_VIEW_MAP_EVENT_FILTER_ENABLED));
    }

    @Override
    public Application getWrapped()
    {
        return wrapped;
    }
}
