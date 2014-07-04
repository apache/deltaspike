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
package org.apache.deltaspike.example.scheduler;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

public class SimpleSchedulerExample
{
    private static final Logger LOG = Logger.getLogger(SimpleSchedulerExample.class.getName());

    private SimpleSchedulerExample()
    {
    }

    public static void main(String[] args) throws InterruptedException
    {
        CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();
        cdiContainer.boot();

        ContextControl contextControl = cdiContainer.getContextControl();
        contextControl.startContext(ApplicationScoped.class);

        GlobalResultHolder globalResultHolder =
            BeanProvider.getContextualReference(GlobalResultHolder.class);

        while (globalResultHolder.getCount() < 100)
        {
            Thread.sleep(500);
            LOG.info("current count: " + globalResultHolder.getCount());
        }
        LOG.info("completed!");

        contextControl.stopContext(ApplicationScoped.class);
        cdiContainer.shutdown();
    }
}
