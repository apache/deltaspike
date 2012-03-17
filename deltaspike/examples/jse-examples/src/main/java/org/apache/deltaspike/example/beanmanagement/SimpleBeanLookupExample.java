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
package org.apache.deltaspike.example.beanmanagement;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.example.echo.DefaultEchoService;
import org.apache.deltaspike.example.echo.EchoService;
import org.apache.deltaspike.example.optional.OptionalService;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.logging.Logger;

/**
 * Example which illustrates the usage of {@inheritDoc BeanProvider}
 */
public class SimpleBeanLookupExample
{
    private static final Logger LOG = Logger.getLogger(SimpleBeanLookupExample.class.getName());

    private SimpleBeanLookupExample()
    {
    }

    /**
     * Entry point
     *
     * @param args currently not used
     */
    public static void main(String[] args)
    {

        CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();
        cdiContainer.boot();

        ContextControl contextControl = cdiContainer.getContextControl();
        contextControl.startContext(ApplicationScoped.class);
        //containerControl.startContexts();

        //or:
        //cdiContainer.start();

        List<EchoService> echoServiceList = BeanProvider.getContextualReferences(EchoService.class, false);

        for (EchoService echoService : echoServiceList)
        {
            LOG.info(echoService.echo("Hello CDI bean!"));
        }

        LOG.info("---");

        echoServiceList = BeanProvider.getContextualReferences(EchoService.class, false, false);

        for (EchoService echoService : echoServiceList)
        {
            LOG.info(echoService.echo("Hello non dependent CDI scoped bean!"));
        }

        LOG.info("---");

        EchoService defaultEchoService = BeanProvider.getContextualReference(DefaultEchoService.class, false);

        LOG.info(defaultEchoService.echo("Hello explicitly resolved CDI bean!"));

        defaultEchoService = BeanProvider.getContextualReference("defaultEchoService", false, EchoService.class);

        LOG.info(defaultEchoService.echo("Hello CDI bean resolved by name!"));

        OptionalService optionalService = BeanProvider.getContextualReference(OptionalService.class, true);

        if (optionalService == null)
        {
            LOG.info("No (optional) implementation found for " + OptionalService.class.getName());
        }
        else
        {
            LOG.severe("Unexpected implementation found: " + optionalService.getClass().getName());
        }

        contextControl.stopContext(ApplicationScoped.class);
        cdiContainer.shutdown();

        //or:
        //containerControl.stopContexts();
        //cdiContainer.shutdownContainer();

        //cdiContainer.stopContext(ApplicationScoped.class); //doesn't work with weld right now - see WELD-1072
    }
}
