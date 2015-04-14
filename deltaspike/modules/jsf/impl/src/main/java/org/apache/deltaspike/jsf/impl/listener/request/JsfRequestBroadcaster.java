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

import org.apache.deltaspike.core.api.literal.DestroyedLiteral;
import org.apache.deltaspike.core.api.literal.InitializedLiteral;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Broadcaster for
 * {@link org.apache.deltaspike.core.api.lifecycle.Initialized}
 * {@link org.apache.deltaspike.core.api.lifecycle.Destroyed}
 * with
 * {@link FacesContext} as event-payload
 * and/or in case of CDI 1.1+
 * {@link javax.enterprise.context.Initialized}
 * {@link javax.enterprise.context.Destroyed}
 * with
 * {@link javax.faces.bean.RequestScoped} as annotation-parameter and
 * {@link FacesContext} as event-payload
 */
@ApplicationScoped
public class JsfRequestBroadcaster implements Deactivatable
{
    @Inject
    private Event<FacesContext> jsfRequestEvent;

    /*
     * annotation-instances for the optional cdi 1.1+ support
     */
    private Annotation initializedAnnotationInstance;
    private Annotation destroyedAnnotationInstance;

    @PostConstruct
    protected void init()
    {
        Map<String, Class> values = new HashMap<String, Class>();
        values.put("value", RequestScoped.class);
        Class<? extends Annotation> initializedAnnotationClass =
            ClassUtils.tryToLoadClassForName("javax.enterprise.context.Initialized");
        if (initializedAnnotationClass != null)
        {
            this.initializedAnnotationInstance = AnnotationInstanceProvider.of(initializedAnnotationClass, values);
        }

        Class<? extends Annotation> destroyedAnnotationClass =
            ClassUtils.tryToLoadClassForName("javax.enterprise.context.Destroyed");
        if (destroyedAnnotationClass != null)
        {
            this.destroyedAnnotationInstance = AnnotationInstanceProvider.of(destroyedAnnotationClass, values);
        }
    }

    /**
     * Broadcasts @Initialized-event(s)
     *
     * @param facesContext current faces-context
     */
    public void broadcastInitializedJsfRequestEvent(FacesContext facesContext)
    {
        this.jsfRequestEvent.select(new InitializedLiteral()).fire(facesContext);

        if (this.initializedAnnotationInstance != null)
        {
            this.jsfRequestEvent.select(this.initializedAnnotationInstance).fire(facesContext);
        }
    }

    /**
     * Broadcasts @Destroyed-event(s)
     *
     * @param facesContext current faces-context
     */
    public void broadcastDestroyedJsfRequestEvent(FacesContext facesContext)
    {
        this.jsfRequestEvent.select(new DestroyedLiteral()).fire(facesContext);

        if (this.destroyedAnnotationInstance != null)
        {
            this.jsfRequestEvent.select(this.destroyedAnnotationInstance).fire(facesContext);
        }
    }
}
