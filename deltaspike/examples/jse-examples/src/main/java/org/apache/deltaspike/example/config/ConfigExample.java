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
package org.apache.deltaspike.example.config;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

public class ConfigExample
{
    private static final Logger LOG = Logger.getLogger(ConfigExample.class.getName());

    private ConfigExample()
    {
    }

    public static void main(String[] args)
    {
        CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();
        cdiContainer.boot();

        ContextControl contextControl = cdiContainer.getContextControl();
        contextControl.startContext(ApplicationScoped.class);

        SettingsBean settingsBean = BeanProvider.getContextualReference(SettingsBean.class, false);

        LOG.info("configured int-value #1: " + settingsBean.getIntProperty1());
        LOG.info("configured long-value #2: " + settingsBean.getProperty2());
        LOG.info("configured inverse-value #2: " + settingsBean.getInverseProperty());
        LOG.info("configured location (custom config): " + settingsBean.getLocationId().name());
        
        cdiContainer.shutdown();
    }
}
