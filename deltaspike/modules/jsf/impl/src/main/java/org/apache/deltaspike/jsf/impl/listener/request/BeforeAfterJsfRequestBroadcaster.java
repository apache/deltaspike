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
package org.apache.deltaspike.jsf.impl.listener.request;

import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.jsf.api.listener.request.AfterJsfRequest;
import org.apache.deltaspike.jsf.api.listener.request.BeforeJsfRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 * Broadcaster for {@link org.apache.deltaspike.jsf.api.listener.request.BeforeJsfRequest} and
 * {@link org.apache.deltaspike.jsf.api.listener.request.AfterJsfRequest}
 */
@ApplicationScoped
public class BeforeAfterJsfRequestBroadcaster implements Deactivatable
{
    @Inject
    @BeforeJsfRequest
    private Event<FacesContext> beforeJsfRequestEvent;

    @Inject
    @AfterJsfRequest
    private Event<FacesContext> afterJsfRequestEvent;

    /**
     * Broadcasts the {@link org.apache.deltaspike.jsf.api.listener.request.BeforeJsfRequest} event
     *
     * @param facesContext current faces-context
     */
    public void broadcastBeforeJsfRequestEvent(FacesContext facesContext)
    {
        this.beforeJsfRequestEvent.fire(facesContext);
    }

    /**
     * Broadcasts the {@link org.apache.deltaspike.jsf.api.listener.request.AfterJsfRequest} event
     *
     * @param facesContext current faces-context
     */
    public void broadcastAfterJsfRequestEvent(FacesContext facesContext)
    {
        this.afterJsfRequestEvent.fire(facesContext);
    }
}
