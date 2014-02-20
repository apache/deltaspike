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

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.deltaspike.core.api.literal.DestroyedLiteral;
import org.apache.deltaspike.core.api.literal.InitializedLiteral;

/**
 * This class listens for HTTP session events and forwards them to the CDI event bus.
 */
public class EventBridgeSessionListener extends EventBroadcaster implements HttpSessionListener
{

    @Override
    public void sessionCreated(HttpSessionEvent se)
    {
        if (isActivated())
        {
            fireEvent(se.getSession(), InitializedLiteral.INSTANCE);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se)
    {
        if (isActivated())
        {
            fireEvent(se.getSession(), DestroyedLiteral.INSTANCE);
        }
    }
}
