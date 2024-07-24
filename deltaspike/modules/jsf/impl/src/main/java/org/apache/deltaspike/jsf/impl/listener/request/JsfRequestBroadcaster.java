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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

/**
 * Broadcaster for
 * {@link jakarta.enterprise.context.Initialized}
 * {@link jakarta.enterprise.context.Destroyed}
 * with
 * {@link jakarta.faces.bean.RequestScoped} as annotation-parameter and
 * {@link FacesContext} as event-payload
 */
@ApplicationScoped
public class JsfRequestBroadcaster implements Deactivatable
{
    @Inject @Initialized(RequestScoped.class) private Event<FacesContext> initEvent;
    @Inject @Destroyed(RequestScoped.class) private Event<FacesContext> destroyedEvent;

    /**
     * Broadcasts @Initialized-event(s)
     *
     * @param facesContext current faces-context
     */
    public void broadcastInitializedJsfRequestEvent(FacesContext facesContext)
    {
        this.initEvent.fire(facesContext);
    }

    /**
     * Broadcasts @Destroyed-event(s)
     *
     * @param facesContext current faces-context
     */
    public void broadcastDestroyedJsfRequestEvent(FacesContext facesContext)
    {
        this.destroyedEvent.fire(facesContext);
    }
}
