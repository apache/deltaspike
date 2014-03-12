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
package org.apache.deltaspike.jsf.api.config;

import java.lang.annotation.Annotation;
import org.apache.deltaspike.core.api.config.DeltaSpikeConfig;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.faces.context.FacesContext;

/**
 * Config for all JSF specific configurations.
 */
@ApplicationScoped
public class JsfModuleConfig implements DeltaSpikeConfig
{
    public static final String CLIENT_WINDOW_CONFIG_KEY = "javax.faces.CLIENT_WINDOW_MODE";
    public static final String CLIENT_WINDOW_CLASS_NAME = "javax.faces.lifecycle.ClientWindow";

    private static final long serialVersionUID = -487295181899986237L;

    protected JsfModuleConfig()
    {
    }

    /**
     * If the initial redirect is enabled, a redirect will be performed for adding the current window-id to the url.
     *
     * @return true for activating it, false otherwise
     */
    public boolean isInitialRedirectEnabled()
    {
        return true;
    }

    /**
     * Per default all faces-messages are preserved for the next rendering process
     * @return true if the messages should be preserved automatically, false otherwise
     */
    public boolean isAlwaysKeepMessages()
    {
        return true;
    }

    /**
     * Per default the current view gets replaced with the error-view (in case of a security-violation).
     * For using a redirect it's needed to return true and using Page.NavigationMode.REDIRECT for @View of the
     * error-view-config.
     * @return true if the navigation-handler should be used in case of a security-violation, false otherwise
     */
    public boolean isAlwaysUseNavigationHandlerOnSecurityViolation()
    {
        return false;
    }

    /**
     * Per default converters get wrapped to restore them properly during a postback (significant without overhead).
     * @return true if converters should be handled as std. CDI beans, false otherwise
     */
    public boolean isContainerManagedConvertersEnabled()
    {
        return true;
    }

    /**
     * Per default validators get wrapped to restore them properly during a postback (significant without overhead).
     * @return true if validators should be handled as std. CDI beans, false otherwise
     */
    public boolean isContainerManagedValidatorsEnabled()
    {
        return true;
    }

    /**
     * If #initialStateMarked (of the component) returns false, a fallback to full state-saving is possible.
     * Therefore it's required to save additional meta-data even with partial state-saving.
     * @return false to restrict additional meta-data required for a possible fallback, true otherwise
     */
    public boolean isFullStateSavingFallbackEnabled()
    {
        return true;
    }

    /**
     * If the window-handling of JSF 2.2+ is enabled,
     * {@link org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig.ClientWindowRenderMode#DELEGATED}
     * will be returned. In all other cases <code>null</code> gets returned as application wide default value.
     * That leads to a default-handling per session (which includes logic for handling bots,...)
     * @return application-default for the window-mode
     */
    public ClientWindowConfig.ClientWindowRenderMode getDefaultWindowMode()
    {
        if (isDelegatedWindowHandlingEnabled())
        {
            return ClientWindowConfig.ClientWindowRenderMode.DELEGATED;
        }
        return null;
    }

    /**
     * Defines the {@link javax.enterprise.Qualifier} which will be used to fire the
     * {@link org.apache.deltaspike.core.api.exception.control.event.ExceptionToCatchEvent}
     * for unhandled JSF exceptions.
     * 
     * @return the {@link javax.enterprise.Qualifier}.
     */
    public Class<? extends Annotation> getExceptionQualifier()
    {
        return Default.class;
    }

    protected boolean isDelegatedWindowHandlingEnabled()
    {
        if (ClassUtils.tryToLoadClassForName(CLIENT_WINDOW_CLASS_NAME) == null)
        {
            return false;
        }

        String configuredWindowHandling = FacesContext.getCurrentInstance().getExternalContext()
                                .getInitParameter(CLIENT_WINDOW_CONFIG_KEY);

        return !(configuredWindowHandling == null || "none".equalsIgnoreCase(configuredWindowHandling.trim()));
    }
}
