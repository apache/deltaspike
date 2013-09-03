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
package org.apache.deltaspike.servlet.impl.event;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.servlet.api.literal.DestroyedLiteral;
import org.apache.deltaspike.servlet.api.literal.InitializedLiteral;

/**
 * This class listens for servlet context events and forwards them to the CDI event bus.
 */
public class EventBridgeContextListener extends EventEmitter implements ServletContextListener, Deactivatable
{

    private final boolean activated;

    public EventBridgeContextListener()
    {
        this.activated = ClassDeactivationUtils.isActivated(getClass());
    }

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        if (activated)
        {
            fireEvent(sce.getServletContext(), InitializedLiteral.INSTANCE);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        if (activated)
        {
            fireEvent(sce.getServletContext(), DestroyedLiteral.INSTANCE);
        }
    }

}
