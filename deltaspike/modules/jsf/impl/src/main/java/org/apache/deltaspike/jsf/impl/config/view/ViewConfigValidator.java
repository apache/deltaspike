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
package org.apache.deltaspike.jsf.impl.config.view;

import org.apache.deltaspike.core.api.config.view.metadata.ConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ViewConfigValidator implements ServletContextListener, Deactivatable
{
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        if (ClassDeactivationUtils.isActivated(getClass()))
        {
            checkViewConfig(sce);
        }
    }

    //allows to re-use it in a custom listener (if this one is deactivated e.g. to change the order)
    protected void checkViewConfig(ServletContextEvent sce)
    {
        ViewConfigResolver viewConfigResolver = BeanProvider.getContextualReference(ViewConfigResolver.class);

        for (ConfigDescriptor configDescriptor : viewConfigResolver.getConfigDescriptors())
        {
            try
            {
                if (sce.getServletContext().getResource(configDescriptor.getPath()) == null)
                {
                    throw new IllegalStateException("path '" + configDescriptor.getPath() +
                        "' is missing, but mapped by: " + configDescriptor.getConfigClass().getName());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace(); //for easier analysis (in combination with several servers)
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
    }
}

