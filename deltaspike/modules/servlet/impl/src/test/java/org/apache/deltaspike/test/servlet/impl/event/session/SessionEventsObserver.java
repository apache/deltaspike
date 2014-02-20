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
package org.apache.deltaspike.test.servlet.impl.event.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.servlet.http.HttpSession;

import org.apache.deltaspike.core.api.lifecycle.Destroyed;
import org.apache.deltaspike.core.api.lifecycle.Initialized;

/**
 * Application scoped observer which listens for {@link HttpSession} events on the CDI event bus.
 */
@ApplicationScoped
public class SessionEventsObserver
{

    private final List<String> eventLog = new ArrayList<String>();

    public void sessionInitialized(@Observes @Initialized HttpSession session)
    {
        eventLog.add("Initialized HttpSession");
    }

    public void sessionDestroyed(@Observes @Destroyed HttpSession session)
    {
        eventLog.add("Destroyed HttpSession");
    }

    public int getEventCount()
    {
        return eventLog.size();
    }

    public List<String> getEventLog()
    {
        return Collections.unmodifiableList(eventLog);
    }

}
