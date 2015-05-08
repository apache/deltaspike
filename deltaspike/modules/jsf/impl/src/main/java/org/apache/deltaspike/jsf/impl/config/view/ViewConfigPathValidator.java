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
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.spi.activation.ClassDeactivator;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.ExceptionUtils;
import org.apache.deltaspike.jsf.api.config.view.Folder;
import org.apache.deltaspike.jsf.api.config.view.View;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewConfigPathValidator implements ServletContextListener, Deactivatable
{
    private static final Logger LOGGER = Logger.getLogger(ViewConfigPathValidator.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        if (ClassDeactivationUtils.isActivated(getClass()))
        {
            ViewConfigResolver viewConfigResolver;

            try
            {
                viewConfigResolver = BeanProvider.getContextualReference(ViewConfigResolver.class);
            }
            catch (Exception e)
            {
                LOGGER.log(Level.WARNING, "Container issue detected -> can't validate view-configs. " +
                    "This exception is usually the effect (but not the reason) of a failed startup. " +
                    "You can deactivate " + getClass().getName() + " via a custom " +
                    ClassDeactivator.class.getName() + " to verify it.", e);
                return;
            }

            List<String> supportedExtensions = new ArrayList<String>();
            supportedExtensions.add(View.Extension.XHTML);
            supportedExtensions.add(View.Extension.JSP);
            validateViewConfigPaths(sce, viewConfigResolver, supportedExtensions);
        }
    }

    //allows to test and re-use it in a custom listener
    // (if a custom listener is needed for supporting custom extensions or
    // this listener is deactivated e.g. to change the order)
    protected void validateViewConfigPaths(ServletContextEvent sce,
                                           ViewConfigResolver viewConfigResolver,
                                           List<String> supportedExtensions)
    {
        for (ConfigDescriptor configDescriptor : viewConfigResolver.getConfigDescriptors())
        {
            try
            {
                if (configDescriptor instanceof ViewConfigDescriptor)
                {
                    //currently other extensions aren't supported
                    String viewId = ((ViewConfigDescriptor) configDescriptor).getViewId();
                    String extension = viewId.substring(viewId.lastIndexOf('.') + 1);

                    if (!supportedExtensions.contains(extension))
                    {
                        continue;
                    }
                }

                if (!isValidPath(sce, configDescriptor))
                {
                    if (configDescriptor instanceof DefaultFolderConfigDescriptor &&
                        !configDescriptor.getConfigClass().isAnnotationPresent(Folder.class))
                    {

                        LOGGER.fine(configDescriptor.getConfigClass().getName() + " looks like a marker interface" +
                            " only used for providing meta-data, because the path " + configDescriptor.getPath() +
                            " doesn't exist and the config-class isn't annotated with " + Folder.class.getName());

                        continue;
                    }

                    throw new IllegalStateException("path '" + configDescriptor.getPath() +
                            "' is missing, but mapped by: " + configDescriptor.getConfigClass().getName());
                }
            }
            catch (Exception e)
            {
                printException(e);
                throw ExceptionUtils.throwAsRuntimeException(e);
            }
        }
    }

    protected boolean isValidPath(ServletContextEvent sce, ConfigDescriptor configDescriptor)
    {
        try
        {
            return sce.getServletContext().getResource(configDescriptor.getPath()) != null;
        }
        catch (MalformedURLException e)
        {
            throw ExceptionUtils.throwAsRuntimeException(e);
        }
    }

    protected void printException(Exception e)
    {
        //for easier analysis (in combination with several servers)
        LOGGER.log(Level.SEVERE, "invalid view-config found", e);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
    }
}

