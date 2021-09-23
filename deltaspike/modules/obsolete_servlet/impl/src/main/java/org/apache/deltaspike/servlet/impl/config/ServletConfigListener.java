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
package org.apache.deltaspike.servlet.impl.config;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.impl.config.ConfigurationExtension;
import org.apache.deltaspike.core.spi.config.ConfigSource;

import jakarta.enterprise.inject.Typed;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Provide the application name in servlet environments.
 */
@WebListener
@Typed
@Exclude
public class ServletConfigListener implements ServletContextListener
{
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        ConfigSource[] configSources = ConfigResolver.getConfigSources();
        for (ConfigSource configSource : configSources)
        {
            if (configSource instanceof ServletConfigSource)
            {
                setServletConfig((ServletConfigSource) configSource, sce);
                return;
            }
        }
    }

    private void setServletConfig(ServletConfigSource configSource, ServletContextEvent sce)
    {
        ServletContext servletContext = sce.getServletContext();
        String servletContextName = servletContext.getServletContextName();
        if (servletContextName != null && servletContextName.length() > 0)
        {
            String oldAppName = ConfigResolver.getPropertyValue(ConfigResolver.DELTASPIKE_APP_NAME_CONFIG);

            // we first need to unregister the old MBean
            // as we don't know whether the CDI Extension or the Servlet Listener comes first.
            // It's simply not defined by the spec :/
            ConfigurationExtension.unRegisterConfigMBean(oldAppName);

            configSource.setPropertyValue(ConfigResolver.DELTASPIKE_APP_NAME_CONFIG, servletContextName);

            // and as we now did set the new name -> register again:
            ConfigurationExtension.registerConfigMBean();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        // nothing to do
    }
}
