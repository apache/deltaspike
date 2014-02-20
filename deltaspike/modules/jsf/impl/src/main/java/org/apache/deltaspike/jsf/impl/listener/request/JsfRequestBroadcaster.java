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

import org.apache.deltaspike.core.api.lifecycle.Destroyed;
import org.apache.deltaspike.core.api.lifecycle.Initialized;
import org.apache.deltaspike.core.spi.activation.Deactivatable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 * Broadcaster for {@link Initialized} and {@link Destroyed}
 */
@ApplicationScoped
public class JsfRequestBroadcaster implements Deactivatable
{
    @Inject
    @Initialized
    private Event<FacesContext> initializedJsfRequestEvent;

    @Inject
    @Destroyed
    private Event<FacesContext> destroyedJsfRequestEvent;

    /**
     * Broadcasts the {@link Initialized} event
     *
     * @param facesContext current faces-context
     */
    public void broadcastInitializedJsfRequestEvent(FacesContext facesContext)
    {
        this.initializedJsfRequestEvent.fire(facesContext);
    }

    /**
     * Broadcasts the {@link Destroyed} event
     *
     * @param facesContext current faces-context
     */
    public void broadcastDestroyedJsfRequestEvent(FacesContext facesContext)
    {
        this.destroyedJsfRequestEvent.fire(facesContext);
    }
}
